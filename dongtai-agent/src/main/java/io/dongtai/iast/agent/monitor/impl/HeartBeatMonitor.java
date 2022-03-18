package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

public class HeartBeatMonitor implements IMonitor {

    private static final String NAME = "HearBeatMonitor";

    @Override
    public String getName() {
        return  Constant.THREAD_PREFIX + NAME;
    }


    @Override
    public void check() throws Exception {
        HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            try {
                this.check();
            } catch (Throwable t) {
                DongTaiLog.warn("Monitor thread checked error, monitor:{}, msg:{}, err:{}", getName(), t.getMessage(), t.getCause());
            }
            ThreadUtils.threadSleep(30);
        }
    }
}
