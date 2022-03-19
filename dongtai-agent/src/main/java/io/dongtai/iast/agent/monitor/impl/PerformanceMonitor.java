package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.monitor.collector.*;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责监控jvm性能状态，如果达到停止阈值，则停止检测引擎；如果达到卸载阈值，则卸载引擎；
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PerformanceMonitor implements IMonitor {
    private final static IastProperties PROPERTIES = IastProperties.getInstance();
    private final static String TOKEN = PROPERTIES.getIastServerToken();
    private final static String START_URL = PROPERTIES.getBaseUrl() + "/api/v1/agent/limit";
    private final static String AGENT_TOKEN = URLEncoder.encode(AgentRegisterReport.getAgentToken());
    private static Integer AGENT_THRESHOLD_VALUE;
    private static Integer CPU_USAGE = 0;
    private static List<PerformanceMetrics> PERFORMANCE_METRICS = new ArrayList<PerformanceMetrics>();

    private static final String NAME = "PerformanceMonitor";
    private final EngineManager engineManager;
    private final List<MetricsKey> needCollectMetrics = new ArrayList<MetricsKey>();

    @Override
    public String getName() {
        return  Constant.THREAD_PREFIX + NAME;
    }


    public PerformanceMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
        configCollectMetrics();
    }

    /**
     * 配置需要收集的指标(todo:通过配置文件初始化)
     */
    private void configCollectMetrics() {
        needCollectMetrics.add(MetricsKey.CPU_USAGE);
        needCollectMetrics.add(MetricsKey.MEM_USAGE);
        needCollectMetrics.add(MetricsKey.MEM_NO_HEAP_USAGE);
        needCollectMetrics.add(MetricsKey.GARBAGE_INFO);
        needCollectMetrics.add(MetricsKey.THREAD_INFO);
    }

    public double memUsedRate() {
        double free = (double) Runtime.getRuntime().freeMemory();
        double max = (double) Runtime.getRuntime().maxMemory();
        return free / max;
    }

    public static Integer getCpuUsage() {
        return CPU_USAGE;
    }

    public static List<PerformanceMetrics> getPerformanceMetrics() {
        return PERFORMANCE_METRICS;
    }

    public static Integer checkThresholdValue() {
        int thresholdValue = 100;
        try {
            String respRaw = getThresholdValue();
            if (respRaw != null && !respRaw.isEmpty()) {
                JSONObject resp = new JSONObject(respRaw);
                JSONArray limitArray = (JSONArray) resp.get("data");
                JSONObject cpuLimit = (JSONObject) limitArray.get(0);
                thresholdValue = Integer.parseInt(cpuLimit.get("value").toString());
            }
        } catch (Exception ignored) {
        }
        return thresholdValue;
    }

    /**
     * 是否到达停止引擎的阈值
     * // 前置状态：0
     * // 切换状态：1
     *
     * @return true, 需要停止；false - 不需要停止
     */
    public boolean isStop(double UsedRate, int preStatus) {
        return UsedRate > AGENT_THRESHOLD_VALUE && (preStatus == 0);
    }

    /**
     * 是否到达启动引擎的阈值
     * // 前置状态：1
     * // 切换状态：0
     *
     * @return true, 需要启动；false - 不需要启动
     */
    public boolean isStart(double UsedRate, int preStatus) {
        return UsedRate < AGENT_THRESHOLD_VALUE && (preStatus == 1);
    }

    /**
     * 状态发生转换时，触发engineManager的操作
     * <p>
     * 状态维护：
     * 0 -> 1 -> 0
     */
    @Override
    public void check() throws Exception {
        // 收集性能指标数据
        final List<PerformanceMetrics> performanceMetrics = collectPerformanceMetrics();
        // 更新本地性能指标记录(用于定期上报)
        updatePerformanceMetrics(performanceMetrics);
        // 检查性能指标(用于熔断降级)
        checkPerformanceMetrics(performanceMetrics);
        int UsedRate = CPU_USAGE;
        PerformanceMonitor.AGENT_THRESHOLD_VALUE = PerformanceMonitor.checkThresholdValue();
        int preStatus = this.engineManager.getRunningStatus();
        if (isStart(UsedRate, preStatus)) {
            this.engineManager.start();
            this.engineManager.setRunningStatus(0);
            DongTaiLog.info("The current CPU usage is " + UsedRate + "%, lower than the threshold " + AGENT_THRESHOLD_VALUE + "%，and the detection engine is starting");
        } else if (isStop(UsedRate, preStatus)) {
            this.engineManager.stop();
            this.engineManager.setRunningStatus(1);
            DongTaiLog.info("The current CPU usage is " + UsedRate + "%, higher than the threshold " + AGENT_THRESHOLD_VALUE + "%，and the detection engine is stopping");
        }
    }

    private void updatePerformanceMetrics(List<PerformanceMetrics> performanceMetrics) {
        for (PerformanceMetrics metrics : performanceMetrics) {
            if (metrics.getMetricsKey() == MetricsKey.CPU_USAGE) {
                final CpuInfoMetrics cpuInfoMetrics = metrics.getMetricsValue(CpuInfoMetrics.class);
                CPU_USAGE = cpuInfoMetrics.getCpuUsagePercentage().intValue();
            }
        }
        PERFORMANCE_METRICS = performanceMetrics;
    }


    /**
     * 收集性能指标
     *
     * @return {@link List}<{@link PerformanceMetrics}>
     */
    private List<PerformanceMetrics> collectPerformanceMetrics() {
        final List<PerformanceMetrics> metricsList = new ArrayList<PerformanceMetrics>();
        for (MetricsKey metricsKey : needCollectMetrics) {
            final MetricsBindCollectorEnum collectorEnum = MetricsBindCollectorEnum.getEnum(metricsKey);
            if (collectorEnum != null) {
                try {
                    IPerformanceCollector collector = collectorEnum.getCollector().newInstance();
                    metricsList.add(collector.getMetrics());
                } catch (Throwable t) {
                    DongTaiLog.error("getPerformanceMetrics failed, collector:{}, err:{}", collectorEnum, t.getMessage());
                }
            }
        }
        return metricsList;
    }

    /**
     * 寻找性能监控熔断器类,反射调用进行性能熔断检查
     */
    private void checkPerformanceMetrics(List<PerformanceMetrics> performanceMetrics) {
        try {
            final Class<?> fallbackManagerClass = EngineManager.getFallbackManagerClass();
            if (fallbackManagerClass == null) {
                return;
            }
            fallbackManagerClass.getMethod("invokePerformanceBreakerCheck", String.class)
                    .invoke(null, SerializeUtils.serializeByList(performanceMetrics));
        } catch (Throwable t) {
            DongTaiLog.error("checkPerformanceMetrics failed, msg:{}, err:{}", t.getMessage(), t.getCause());
        }
    }

    private static String getThresholdValue() {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(PerformanceMonitor.START_URL + "?agentName=" + AGENT_TOKEN);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json; charset=utf-8");
            connection.setRequestProperty("Authorization", "Token " + TOKEN);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sbf = new StringBuilder();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
            }
        } catch (IOException ignored) {
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
        return result;
    }

    static {
        AGENT_THRESHOLD_VALUE = 100;
    }

    @Override
    public void run() {
        while(!MonitorDaemonThread.isExit) {
            try {
                this.check();
            } catch (Throwable t) {
                DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
            }
            ThreadUtils.threadSleep(30);
        }
    }
}
