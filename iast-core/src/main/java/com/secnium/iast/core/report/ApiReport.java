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

    public static void sendReport(List<ApiDataModel> apiList) {
         String report = createReport(apiList);
         EngineManager.sendNewReport(report);
    }

    private static String createReport(List<ApiDataModel> apiList) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray apiData = new JSONArray();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_API);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);
        detail.put(ReportConstant.AGENT_ID,AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.API_DATA, apiData);
        for (ApiDataModel apiDataModel:apiList
             ) {
            JSONObject api = new JSONObject();
            apiData.put(api);
            api.put(ReportConstant.API_DATA_URI,apiDataModel.getUrl());
            JSONArray methodsjson = new JSONArray();
            api.put(ReportConstant.API_DATA_METHOD,methodsjson);
            String[] methods = apiDataModel.getMethod();
            for (String method:methods
                 ) {
                methodsjson.put(method);
            }
            api.put(ReportConstant.API_DATA_METHOD,apiDataModel.getMethod());
            api.put(ReportConstant.API_DATA_CLASS,apiDataModel.getClazz());
            List<Map<String, String>> parameters = apiDataModel.getParameters();
            JSONArray parametersJson = new JSONArray();
            api.put(ReportConstant.API_DATA_PARAMETERS,parametersJson);
            for (Map<String,String> parameter:parameters
                 ) {
                JSONObject parameterjson = new JSONObject();
                parametersJson.put(parameterjson);
                parameterjson.put(ReportConstant.API_DATA_PARAMETER_NAME,parameter.get(ReportConstant.API_DATA_PARAMETER_NAME));
                parameterjson.put(ReportConstant.API_DATA_PARAMETER_TYPE,parameter.get(ReportConstant.API_DATA_PARAMETER_TYPE));
                parameterjson.put(ReportConstant.API_DATA_PARAMETER_ANNOTATION,parameter.get(ReportConstant.API_DATA_PARAMETER_ANNOTATION));
            }
            api.put(ReportConstant.API_DATA_RETURN,apiDataModel.getReturnType());
            api.put(ReportConstant.API_DATA_FILE,apiDataModel.getFile());
            api.put(ReportConstant.API_DATA_CONTROLLER,apiDataModel.getController());
            api.put(ReportConstant.API_DATA_DESCRIPTION,apiDataModel.getDescription());
        }
        return report.toString();
    }

}
