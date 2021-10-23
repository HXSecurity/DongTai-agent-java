package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.LogUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class StartUpTimeReport {

    public static void sendReport(Integer id, Integer startUpTime) {
        String token = PropertyUtils.getInstance().getIastServerToken();
        String httpUrl = PropertyUtils.getInstance().getBaseUrl() + "/api/v1/agent/startuptime";
        String report = createReport(id, startUpTime);
        sendPost(httpUrl, token, report);
    }

    private static String createReport(Integer id, Integer startUpTime) {
        JSONObject report = new JSONObject();
        report.put(ReportConstant.AGENT_ID, id);
        report.put(ReportConstant.STARTUP_TIME, startUpTime);
        return report.toString();
    }

    public static void sendPost(String httpUrl, String token, String report) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = null;
        try{
            URL url = new URL(httpUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Token " + token);
            out = new OutputStreamWriter(conn.getOutputStream());
            out.write(report);
            out.flush();
            out.close();
            if (200 == conn.getResponseCode()){
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null){
                    result.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
