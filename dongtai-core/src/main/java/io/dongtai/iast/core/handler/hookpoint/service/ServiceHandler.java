package io.dongtai.iast.core.handler.hookpoint.service;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.service.url.*;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.log.DongTaiLog;

import java.util.*;

public class ServiceHandler {
    private static final String KAFKA_URL_HANDLER = "KafkaUrlHandler";
    private static final String SIMPLE_URL_HANDLER = "SimpleUrlHandler";
    private static final Map<String, Boolean> uniqMap = new HashMap<String, Boolean>();

    public static void reportService(String category, String type, String host, String port, String handler) {
        try {
            ServiceUrlHandler h;
            if (KAFKA_URL_HANDLER.equals(handler)) {
                h = new KafkaUrlHandler();
            } else {
                h = new SimpleUrlHandler();
            }

            List<ServiceUrl> srvList = h.processUrl(host, port);
            for (ServiceUrl srv : srvList) {
                reportSingleService(category, type, srv.getHost(), srv.getPort());
            }
        } catch (Throwable e) {
            DongTaiLog.trace("report service failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }

    public static void reportSingleService(String category, String type, String host, String port) {
        if (!uniqCheck(category, type, host, port)) {
            return;
        }
        String report = createReport(category, type, host, port);
        ThreadPools.sendReport(ApiPath.REPORT_UPLOAD, report);
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

        report.put(ReportKey.TYPE, ReportType.SERVICE);
        report.put(ReportKey.DETAIL, detail);

        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());

        srv.put("serviceType", type);
        srv.put("address", host);
        srv.put("port", port);
        ArrayList<JSONObject> srvList = new ArrayList<JSONObject>();
        srvList.add(srv);
        detail.put("serviceData", srvList);

        return report.toString();
    }
}
