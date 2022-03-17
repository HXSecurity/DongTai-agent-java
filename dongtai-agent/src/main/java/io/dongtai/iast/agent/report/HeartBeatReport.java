package io.dongtai.iast.agent.report;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.PerformanceMonitor;
import io.dongtai.iast.agent.util.ByteUtils;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatReport {

    public static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(Constant.KEY_UPDATE_REPORT, Constant.REPORT_HEART_BEAT);
        report.put(Constant.KEY_REPORT_VALUE, detail);

        detail.put(Constant.KEY_AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(Constant.KEY_MEMORY, getMemInfo());
        detail.put(Constant.KEY_CPU, readCpuInfo());
        detail.put(Constant.KEY_DISK,getDiskInfo());
        detail.put(Constant.KEY_PERFORMANCE, readRecentlyPerformanceMetrics());
        detail.put(Constant.KEY_CORE_INSTALLED, EngineManager.checkCoreIsInstalled() ? 1 : 0);
        detail.put(Constant.KEY_CORE_RUNNING, EngineManager.checkCoreIsRunning() ? 1 : 0);
        detail.put(Constant.KEY_RETURN_QUEUE, 0);

        return report.toString();
    }

    /**
     * Query CPU usage
     *
     * @return CPU usage, eg: 20%
     */
    private static String readCpuInfo() {
        JSONObject cpuInfo = new JSONObject();
        cpuInfo.put("rate", PerformanceMonitor.getCpuUsage());
        return cpuInfo.toString();
    }

    /**
     * 读取最近收集的性能指标
     */
    private static String readRecentlyPerformanceMetrics() {
        final List<PerformanceMetrics> performanceMetricsList = PerformanceMonitor.getPerformanceMetrics();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        JSONArray metricsList = new JSONArray();
        for (PerformanceMetrics metrics : performanceMetricsList) {
            JSONObject metricsObj = new JSONObject();
            metricsObj.put("metricsKey", metrics.getMetricsKey().getKey());
            metricsObj.put("collectDate", metrics.getCollectDate() != null ? simpleDateFormat.format(metrics.getCollectDate()) : null);
            metricsObj.put("metricsValue", new JSONObject(metrics.getMetricsValue(metrics.getMetricsKey().getValueType())));
            metricsList.put(metricsObj);
        }
        return metricsList.toString();
    }

    /**
     * 实时获取内存信息
     */
    private static String getMemInfo() {
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        JSONObject memoryReport = new JSONObject();
        memoryReport.put("total", ByteUtils.formatByteSize(memoryUsage.getMax()));
        memoryReport.put("use", ByteUtils.formatByteSize(memoryUsage.getUsed()));
        memoryReport.put("rate", memoryUsage.getUsed() / memoryUsage.getMax());
        return memoryReport.toString();
    }

    /**
     * 实时读取磁盘信息
     *
     * @return
     */
    public static String getDiskInfo() {
        return "{}";
    }



}
