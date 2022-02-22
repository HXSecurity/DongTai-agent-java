package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.monitor.IMonitor;
import io.dongtai.iast.agent.report.HeartBeatReport;
import io.dongtai.iast.agent.util.http.HttpClientUtils;

public class HeartBeatMonitor implements IMonitor {
    @Override
    public void check() {
        try {
            HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
