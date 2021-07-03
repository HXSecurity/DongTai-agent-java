package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractNormalVulScan implements IVulScan {
    private static final ServerDetect SERVER_DETECT = ServerDetect.getInstance();

    public void sendReport(StackTraceElement stack, String vulType) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.AGENT_VERSION, ReportConstant.AGENT_VERSION_VALUE);
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.VULN_TYPE, vulType);
        detail.put(ReportConstant.COMMON_APP_NAME, requestMeta.get("contextPath"));
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, requestMeta.get("scheme"));
        detail.put(ReportConstant.COMMON_SERVER_NAME, requestMeta.get("serverName"));
        detail.put(ReportConstant.COMMON_SERVER_PORT, requestMeta.get("serverPort"));
        detail.put(ReportConstant.CONTAINER, SERVER_DETECT.getServeName());
        detail.put(ReportConstant.CONTAINER_PATH, SERVER_DETECT.getWebServerPath());
        detail.put(ReportConstant.COMMON_REMOTE_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.COMMON_HTTP_SECURE, requestMeta.get("secure"));
        detail.put(ReportConstant.COMMON_HTTP_URL, requestMeta.get("requestURL").toString());
        detail.put(ReportConstant.COMMON_HTTP_URI, requestMeta.get("requestURI"));
        detail.put(ReportConstant.COMMON_HTTP_METHOD, requestMeta.get("method"));
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Encoder.encodeBase64String(requestMeta.get("headers").toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_BODY, requestMeta.get("body"));
        detail.put(ReportConstant.COMMON_HTTP_CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.COMMON_HTTP_REPLAY_REQUEST, requestMeta.get("replay-request"));
        detail.put(ReportConstant.VULN_CALLER, stack);

        EngineManager.sendNewReport(report.toString());
    }

    protected StackTraceElement getLatestStack() {
        return StackUtils.getLatestStack(10);
    }
}
