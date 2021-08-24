package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.replay.HttpRequestReplay;
import com.secnium.iast.core.util.ByteUtils;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatSender extends Thread {
    private final Logger logger = LogUtils.getLogger(getClass());

    private static String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.HEART_BEAT_MEMORY, getMemInfo());
        detail.put(ReportConstant.HEART_BEAT_CPU, readCpuInfo());
        detail.put(ReportConstant.HEART_BEAT_DISK, getDiskInfo());
        detail.put(ReportConstant.HEART_BEAT_REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.REPORT_QUEUE, EngineManager.getReportQueueSize());
        detail.put(ReportConstant.METHOD_QUEUE, EngineManager.getMethodReportQueueSize());
        detail.put(ReportConstant.REPLAY_QUEUE, EngineManager.getReplayQueueSize());

        return report.toString();
    }

    /**
     * 实时获取CPU信息
     *
     * @return
     */
    public static String readCpuInfo() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        JSONObject cpuInfo = new JSONObject();
        cpuInfo.put("rate", (int) (operatingSystemMXBean.getSystemLoadAverage() * 10));
        return cpuInfo.toString();
    }

    /**
     * 实时获取内存信息
     */
    public static String getMemInfo() {
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

    @Override
    public void run() {
        boolean isRunning = EngineManager.isLingzhiRunning();
        if (isRunning) {
            EngineManager.turnOffLingzhi();
        }
        try {
            String report = generateHeartBeatMsg();
            StringBuilder response = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
            HttpRequestReplay.sendReplayRequest(response);
        } catch (IOException e) {
            logger.error("report error reason: ", e);
        } catch (Exception e) {
            logger.error("report error, reason: ", e);
        }

        if (isRunning) {
            EngineManager.turnOnLingzhi();
        }
    }
}
