package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.HttpClientUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.log.DongTaiLog;

public class HeartBeatMonitor implements IMonitor {

    private static final String NAME = "HearBeatMonitor";

    @Override
    public String getName() {
        return AgentConstant.THREAD_NAME_PREFIX + NAME;
    }


    @Override
    public void check() throws Exception {
        HttpClientUtils.sendPost(ApiPath.REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
    }

    @Override
    public void run() {
        try {
            while (!MonitorDaemonThread.isExit) {
                try {
                    this.check();
                } catch (Throwable t) {
                    DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
                }
                ThreadUtils.threadSleep(30);
            }
        } catch (Throwable t) {
            DongTaiLog.debug("HeartBeatMonitor interrupted, msg:{}", t.getMessage());
        }
    }
}
