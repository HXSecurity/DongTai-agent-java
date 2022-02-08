package com.secnium.iast.agent.report;

import com.secnium.iast.agent.Constant;
import com.secnium.iast.agent.monitor.PerformanceMonitor;
import com.secnium.iast.agent.util.ByteUtils;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

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
