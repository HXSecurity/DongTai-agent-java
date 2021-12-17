package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.HttpClientUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author owefsad
 */
public class StartUpTimeReport {

    public static void sendReport(Integer id, Integer startUpTime) {
        JSONObject report = new JSONObject();
        report.put(ReportConstant.AGENT_ID, id);
        report.put(ReportConstant.STARTUP_TIME, startUpTime);

        HttpClientUtils.sendJsonPost(PropertyUtils.getInstance().getBaseUrl()+"/api/v1/agent/startuptime",report.toString());
    }

}
