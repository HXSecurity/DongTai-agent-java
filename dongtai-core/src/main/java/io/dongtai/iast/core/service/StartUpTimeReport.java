package io.dongtai.iast.core.service;

import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.service.ThreadPools;
import org.json.JSONObject;

/**
 * @author owefsad
 */
public class StartUpTimeReport {

    public static void sendReport(Integer id, Integer startUpTime) {
        JSONObject report = new JSONObject();
        report.put(ReportConstant.AGENT_ID, id);
        report.put(ReportConstant.STARTUP_TIME, startUpTime);

        ThreadPools.sendReport("/api/v1/agent/gzipstartuptime", report.toString());
    }
}
