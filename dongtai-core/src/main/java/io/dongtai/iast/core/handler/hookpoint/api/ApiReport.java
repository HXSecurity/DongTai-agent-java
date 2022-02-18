package io.dongtai.iast.core.handler.hookpoint.api;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.Constants;
import java.util.Map;

import io.dongtai.iast.core.service.ThreadPools;
import org.json.JSONObject;

/**
 * send api sitemap
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiReport {

    public static void sendReport(Map<String, Object> apiList) {
        String report = createReport(apiList);
        ThreadPools.sendReport(Constants.API_REPORT_UPLOAD, report);
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
