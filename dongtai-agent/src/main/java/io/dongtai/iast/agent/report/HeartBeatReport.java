package io.dongtai.iast.agent.report;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.agent.monitor.impl.PerformanceMonitor;
import io.dongtai.iast.agent.util.ByteUtils;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.state.AgentState;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatReport {

    public static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportKey.TYPE, ReportType.HEART_BEAT);
        report.put(ReportKey.DETAIL, detail);

        detail.put(ReportKey.AGENT_ID, AgentRegisterReport.getAgentId());
        detail.put("memory", getMemInfo());
        detail.put("cpu", readCpuInfo());
        detail.put("disk", getDiskInfo());
        // TODO: refactor agent metrics report
        // do not fetch replay request
        detail.put(ReportKey.RETURN_QUEUE, 0);

        return report.toString();
    }

    public static String generateAgentActualActionMsg(AgentState agentState) {
        JSONObject detail = new JSONObject();
        detail.put(ReportKey.AGENT_ID, AgentRegisterReport.getAgentId());
        detail.put("actualRunningStatus", agentState.getState().getCode());
        detail.put("stateStatus", agentState.getCause().getCode());
        return detail.toString();
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
        long total = 0;
        long use = 0;
        double rate = 0;
        MemoryUsageMetrics memoryUsageMetrics = PerformanceMonitor.getMemoryUsage();
        if (memoryUsageMetrics != null) {
            total = memoryUsageMetrics.getTrulyMaxMem();
            use = memoryUsageMetrics.getUsed();
            rate = memoryUsageMetrics.getMemUsagePercentage();
        }

        JSONObject memoryReport = new JSONObject();
        memoryReport.put("total", ByteUtils.formatByteSize(total));
        memoryReport.put("use", ByteUtils.formatByteSize(use));
        memoryReport.put("rate", (int) rate);
        return memoryReport.toString();
    }

    /**
     * 实时读取磁盘信息
     *
     * @return
     */
    public static String getDiskInfo() {
        JSONObject diskInfo = new JSONObject();
        diskInfo.put("rate", PerformanceMonitor.getDiskUsage());
        return diskInfo.toString();
    }
}
