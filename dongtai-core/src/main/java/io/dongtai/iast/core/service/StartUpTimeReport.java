package io.dongtai.iast.core.service;

import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.constants.ReportKey;
import org.json.JSONObject;

/**
 * @author owefsad
 */
public class StartUpTimeReport {

    public static void sendReport(Integer id, Integer startUpTime) {
        JSONObject report = new JSONObject();
        report.put(ReportKey.AGENT_ID, id);
        report.put("startupTime", startUpTime);

        ThreadPools.sendReport(ApiPath.STARTUP_TIME, report.toString());
    }
}
