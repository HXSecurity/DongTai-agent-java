package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.util.LogUtils;
import com.secnium.iast.agent.IastProperties;
import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.report.AgentRegisterReport;
import org.json.JSONArray;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

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

    private final EngineManager engineManager;

    public PerformanceMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public double memUsedRate() {
        double free = (double) Runtime.getRuntime().freeMemory();
        double max = (double) Runtime.getRuntime().maxMemory();
        return free / max;
    }

    public static Integer getCpuUsage(){
        return CPU_USAGE;
    }

    public Integer cpuUsedRate() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ignored) {
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        CPU_USAGE = (int)((1.0 - (idle * 1.0 / totalCpu)) * 100);
        return CPU_USAGE;
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
    public void check() {
        PerformanceMonitor.AGENT_THRESHOLD_VALUE = PerformanceMonitor.checkThresholdValue();
        int UsedRate = cpuUsedRate();
        int preStatus = this.engineManager.getRunningStatus();
        if (isStart(UsedRate, preStatus)) {
            this.engineManager.start();
            this.engineManager.setRunningStatus(0);
            LogUtils.info("The current CPU usage is " + UsedRate + "%, lower than the threshold " + AGENT_THRESHOLD_VALUE + "%，and the detection engine is starting");
        } else if (isStop(UsedRate, preStatus)) {
            this.engineManager.stop();
            this.engineManager.setRunningStatus(1);
            LogUtils.info("The current CPU usage is " + UsedRate + "%, higher than the threshold " + AGENT_THRESHOLD_VALUE + "%，and the detection engine is stopping");
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
}
