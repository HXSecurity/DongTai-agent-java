package io.dongtai.iast.core.service;

import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 生成并生成错误日志报告
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ErrorLogReport {

    private static final boolean ENABLE_UPLOAD = "true".equals(System.getProperty("dongtai.error.upload", "false"));

    public static void sendErrorLog(String errorLog) {
        String report = createReport(errorLog);
        ThreadPools.sendReport(ApiPath.REPORT_UPLOAD, report);

    }

    public static void sendErrorLog(Throwable t) {
        DongTaiLog.error(t);
        if (ENABLE_UPLOAD) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            DongTaiLog.error(sw.toString());
            sendErrorLog(sw.toString());
        }
    }

    private static String createReport(String errorLog) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportKey.TYPE, ReportType.ERROR_LOG);
        report.put(ReportKey.DETAIL, detail);

        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put("log", errorLog);

        return report.toString();
    }

}
