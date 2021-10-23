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
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.concurrent.TimeUnit;

/**
 * 心跳机制实现，默认30s
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HeartBeatSender extends Thread {
    private final Logger logger = LogUtils.getLogger(getClass());
    private static HeartBeatSender INSTANCE = null;

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as
     * {@code (null, null, gname)}, where {@code gname} is a newly generated
     * name. Automatically generated names are of the form
     * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
     */
    public HeartBeatSender() {
    }

    private static HeartBeatSender getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new HeartBeatSender();
        }
        return INSTANCE;
    }


    private String generateHeartBeatMsg() {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_HEART_BEAT);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.MEMORY, getMemInfo());
        detail.put(ReportConstant.CPU, readCpuInfo());
        detail.put(ReportConstant.DISK, getDiskInfo());
        detail.put(ReportConstant.REQ_COUNT, EngineManager.getRequestCount());
        detail.put(ReportConstant.REPORT_QUEUE, EngineManager.getReportQueueSize());
        detail.put(ReportConstant.METHOD_QUEUE, EngineManager.getMethodReportQueueSize());
        detail.put(ReportConstant.REPLAY_QUEUE, EngineManager.getReplayQueueSize());

        return report.toString();
    }


    /**
     * Query CPU usage
     *
     * @return CPU usage, eg: 20%
     */
    private String readCpuInfo() {
        JSONObject cpuInfo = new JSONObject();
        cpuInfo.put("rate", getCpuInfo());
        return cpuInfo.toString();
    }

    private Integer getCpuInfo() {
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
        double rate = (1.0 - (idle * 1.0 / totalCpu)) * 100;
        return (int) rate;
    }

    /**
     * 实时获取内存信息
     */
    private String getMemInfo() {
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
    public String getDiskInfo() {
        return "{}";
    }

    @Override
    public void run() {
        if (EngineManager.isTransforming()) {
            return;
        }
        boolean isRunning = EngineManager.isLingzhiRunning();
        if (isRunning) {
            EngineManager.turnOffLingzhi();
        }

        try {
            HeartBeatSender heartBeatSender = HeartBeatSender.getInstance();
            StringBuilder response = HttpClientUtils.sendPost(
                    Constants.API_REPORT_UPLOAD,
                    heartBeatSender.generateHeartBeatMsg()
            );
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
