package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.Constants;
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
        ThreadPools.send(Constants.API_REPORT_UPLOAD, report);
    }

    public static void sendErrorLog(Throwable t) {
        if (ENABLE_UPLOAD) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sendErrorLog(t);
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
