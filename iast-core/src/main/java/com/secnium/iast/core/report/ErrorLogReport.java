package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Constants;
import org.json.JSONObject;

/**
 * 生成并生成错误日志报告
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ErrorLogReport {

    public static void sendErrorLog(String errorLog) {
        String report = createReport(errorLog);
        ReportThread.send(Constants.API_REPORT_UPLOAD, report);
    }

    private static String createReport(String errorLog) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_ERROR_LOG);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.ERROR_LOG_DETAIL, errorLog);

        return report.toString();
    }

}
