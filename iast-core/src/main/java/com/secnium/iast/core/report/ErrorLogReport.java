package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import org.json.JSONObject;

/**
 * 生成并生成错误日志报告
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ErrorLogReport {

    public static void sendErrorLog(String errorLog) {
        String report = createReport(errorLog);
        EngineManager.sendNewReport(report);
    }

    private static String createReport(String errorLog) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_ERROR_LOG);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.PROJECT_NAME, AgentRegisterReport.getProjectName());
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.COMMON_APP_NAME, EngineManager.REQUEST_CONTEXT.get() == null ? "" : EngineManager.REQUEST_CONTEXT.get().get("contextPath"));
        detail.put(ReportConstant.HEART_BEAT_WEB_SERVER_PATH, HeartBeatReport.getWebServerPath());
        detail.put(ReportConstant.ERROR_LOG_DETAIL, errorLog);

        return report.toString();
    }

}
