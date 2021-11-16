package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.Constant;
import com.secnium.iast.agent.report.HeartBeatReport;
import com.secnium.iast.agent.util.http.HttpClientUtils;

public class HeartBeatMonitor implements IMonitor{
    @Override
    public void check() {
        try {
            HttpClientUtils.sendPost(Constant.API_REPORT_UPLOAD, HeartBeatReport.generateHeartBeatMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
