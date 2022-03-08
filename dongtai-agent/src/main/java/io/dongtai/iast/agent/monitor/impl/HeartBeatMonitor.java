package io.dongtai.iast.agent.monitor.impl;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.iast.agent.util.http.HttpClientUtils;

public class HeartBeatMonitor implements IMonitor {

    public final String name = "HearBeatMonitor";

    @Override
    public String getName() {
        return  Constant.THREAD_PREFIX + name;
    }


    @Override
    public void check() {
        try {
            HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!MonitorDaemonThread.isExit) {
            this.check();
            ThreadUtils.threadSleep(30);
        }
    }
}
