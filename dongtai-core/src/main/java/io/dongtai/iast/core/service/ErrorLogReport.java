package io.dongtai.iast.core.service;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.utils.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONObject;

/**
 * 生成并生成错误日志报告
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ErrorLogReport {

    private static final boolean ENABLE_UPLOAD = "true".equals(System.getProperty("dongtai.error.upload", "false"));

    public static void sendErrorLog(String errorLog) {
        String report = createReport(errorLog);
        ThreadPools.sendReport(Constants.API_REPORT_UPLOAD, report);
    }

    public static void sendErrorLog(Throwable t) {
        t.printStackTrace();
        if (ENABLE_UPLOAD) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sendErrorLog(sw.toString());
        }
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
