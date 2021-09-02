package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import org.json.JSONArray;
import org.json.JSONObject;
import com.secnium.iast.core.handler.models.ApiDataModel;

import java.util.List;
import java.util.Map;

/**
 * 发送应用接口
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiReport {

    public static void sendReport(String apiList) {
         String report = createReport(apiList);
         EngineManager.sendNewReport(report);
    }

    private static String createReport(String apiList) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray api = new JSONArray();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_API);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);
        detail.put(ReportConstant.AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.API_DATA, api);
        String result = report.toString();
        result = result.replace("\"api_data\":[]", "\"api_data\":" + apiList).replace("url","uri").replace("returnType","return_type").replace("\"method\":[]","\"method\":[\"GET\",\"POST\"]");
        return result;
    }

}
