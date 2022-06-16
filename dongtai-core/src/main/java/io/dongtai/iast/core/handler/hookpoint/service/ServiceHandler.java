package io.dongtai.iast.core.handler.hookpoint.service;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.service.url.*;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.Constants;
import org.json.JSONObject;

import java.util.*;

public class ServiceHandler {
    private static Map<String, Boolean> uniqMap = new HashMap<String, Boolean>();

    public static void reportService(String category, String type, String host, String port, String handler) {
        try {
            ServiceUrlHandler h;
            if ("KafkaUrlHandler".equals(handler)) {
                h = new KafkaUrlHandler();
            } else {
                h = new SimpleUrlHandler();
            }

            List<ServiceUrl> srvList = h.processUrl(host, port);
            for (ServiceUrl srv : srvList) {
                reportSingleService(category, type, srv.getHost(), srv.getPort());
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    public static void reportSingleService(String category, String type, String host, String port) {
        if (!uniqCheck(category, type, host, port)) {
            return;
        }
        String report = createReport(category, type, host, port);
        ThreadPools.sendReport(Constants.API_REPORT_UPLOAD, report);
    }

    private static Boolean uniqCheck(String category, String type, String host, String port) {
        Integer agentId = EngineManager.getAgentId();
        String key = String.format("%s://%d@%s:%s", type, agentId, host, port);
        if (uniqMap.containsKey(key)) {
            return false;
        } else {
            uniqMap.put(key, true);
            return true;
        }
    }

    private static String createReport(String category, String type, String host, String port) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONObject srv = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_SERVICE);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());

        srv.put(ReportConstant.KEY_SERVICE_TYPE, type);
        srv.put(ReportConstant.KEY_SERVICE_ADDRESS, host);
        srv.put(ReportConstant.KEY_SERVICE_PORT, port);
        ArrayList<JSONObject> srvList = new ArrayList<JSONObject>();
        srvList.add(srv);
        detail.put(ReportConstant.KEY_SERVICE_DATA, srvList);

        return report.toString();
    }
}
