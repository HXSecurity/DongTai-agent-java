package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Constants;
import java.util.Map;
import org.json.JSONObject;

/**
 * send api sitemap
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiReport {

    public static void sendReport(Map<String, Object> apiList) {
        String report = createReport(apiList);
        ReportThread.send(Constants.API_REPORT_UPLOAD, report);
    }

    private static String createReport(Map<String, Object> apiList) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_API);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);
        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        JSONObject apiListJson = new JSONObject(apiList);
        detail.put(ReportConstant.API_DATA, apiListJson.get(ReportConstant.API_DATA));
        return report.toString();
    }

}
