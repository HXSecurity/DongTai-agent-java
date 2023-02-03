package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

public class HeartBeatMonitor implements IMonitor {

    private static final String NAME = "HearBeatMonitor";

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }


    @Override
    public void check() {
        try {
            HttpClientUtils.sendPost(ApiPath.REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
        } catch (Throwable t) {
            DongTaiLog.warn(ErrorCode.AGENT_MONITOR_THREAD_CHECK_FAILED, getName(), t);
        }
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                this.check();
                ThreadUtils.threadSleep(30);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("{} interrupted: {}", getName(), t.getMessage());
        }
    }
}
