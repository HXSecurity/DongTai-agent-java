package io.dongtai.iast.core.handler.hookpoint.api;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.service.ThreadPools;

import java.util.Map;

/**
 * send api sitemap
 *
 * @author niuerzhuang@huoxian.cn
 */
public class ApiReport {

    public static void sendReport(Map<String, Object> apiList) {
        String report = createReport(apiList);
        ThreadPools.sendReport(ApiPath.REPORT_UPLOAD, report);
    }

    private static String createReport(Map<String, Object> apiList) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportKey.TYPE, ReportType.API);
        report.put(ReportKey.DETAIL, detail);
        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        JSONObject apiListJson = new JSONObject(apiList);
        detail.put(ReportKey.API_DATA, apiListJson.get(ReportKey.API_DATA));
        return report.toString();
    }

}
