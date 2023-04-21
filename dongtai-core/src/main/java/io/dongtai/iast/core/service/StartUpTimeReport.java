package io.dongtai.iast.core.service;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.constants.ReportKey;

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
