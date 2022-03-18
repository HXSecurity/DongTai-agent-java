package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DongTaiThreadMonitor implements IMonitor {
    private static final String NAME = "DongTaiThreadMonitor";

    /**
     * 高cpu百分比
     */
    private static final double HIGH_CPU_PERCENTAGE = 90.0;

    @Override
    public String getName() {
        return Constant.THREAD_PREFIX + NAME;
    }

    @Override
    public void check() throws Exception {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        List<ThreadInfoMetrics.ThreadInfo> dongTaiThreads = ThreadUtils.getDongTaiThreads();
        Set<String> notExistThreads = new HashSet<String>();
        List<ThreadInfoMetrics.ThreadInfo> highCpuDaemonThreads = new ArrayList<ThreadInfoMetrics.ThreadInfo>();
        for (IMonitor monitor : MonitorDaemonThread.monitorTasks) {
            boolean isMonitorAlive = false;
            for (ThreadInfoMetrics.ThreadInfo dongTaiThread : dongTaiThreads) {
                if (monitor.getName() != null && monitor.getName().equals(dongTaiThread.getName())) {
                    // 判断监控线程是否存活
                    isMonitorAlive = true;
                    // 判断监控线程是否高cpu占用
                    if (dongTaiThread.getCpuUsage() >= HIGH_CPU_PERCENTAGE) {
                        highCpuDaemonThreads.add(dongTaiThread);
                    }
                }
            }
            if (!isMonitorAlive) {
                notExistThreads.add(monitor.getName());
            }
        }
        // 生成报告
        if (notExistThreads.size() > 0) {
            detail.put(Constant.KEY_NOT_EXIST_THREADS, notExistThreads.toString());
        }
        if (highCpuDaemonThreads.size() > 0) {
            final JSONArray highCpuThreadJsonList = new JSONArray();
            for (ThreadInfoMetrics.ThreadInfo each : highCpuDaemonThreads) {
                final JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", each.getId());
                jsonObj.put("name", each.getName());
                jsonObj.put("cpuTime", each.getCpuTime());
                jsonObj.put("cpuUsage", each.getCpuUsage());
                // kill指定线程
                jsonObj.put("killRes", ThreadUtils.killDongTaiThread(each.getId()));
                highCpuThreadJsonList.put(jsonObj);
            }
            detail.put(Constant.KEY_HIGH_CPU_THREADS, highCpuThreadJsonList);
        }
        if (notExistThreads.size() > 0 || highCpuDaemonThreads.size() > 0) {
            report.put(Constant.KEY_UPDATE_REPORT, Constant.REPORT_ERROR_THREAD);
            report.put(Constant.KEY_REPORT_VALUE, detail);
            HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD, report.toString());
        }
    }


    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            // 延迟启动DongTaiThreadMonitor，以防首次加载时其他Monitor未能启动的情形出现。
            ThreadUtils.threadSleep(10);
            try {
                this.check();
            } catch (Throwable t) {
                DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
            }
            ThreadUtils.threadSleep(50);
        }
    }

}
