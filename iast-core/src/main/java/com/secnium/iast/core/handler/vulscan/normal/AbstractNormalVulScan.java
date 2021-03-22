package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.base64.Base64Utils;
import com.secnium.iast.core.util.http.HttpRequest;
import org.json.JSONObject;

public abstract class AbstractNormalVulScan implements IVulScan {
    private static final ServerDetect SERVER_DETECT = ServerDetect.getInstance();

    public void sendReport(StackTraceElement stack, String vulType) {
        HttpRequest request = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.AGENT_VERSION, ReportConstant.AGENT_VERSION_VALUE);
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);

        detail.put(ReportConstant.VULN_TYPE, vulType);
        detail.put(ReportConstant.COMMON_APP_NAME, request.getContextPath());
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, request.getProtocol());
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, request.getScheme());
        detail.put(ReportConstant.COMMON_SERVER_NAME, request.getServerName());
        detail.put(ReportConstant.COMMON_SERVER_PORT, request.getServerPort());
        detail.put(ReportConstant.CONTAINER, SERVER_DETECT.getServeName());
        detail.put(ReportConstant.CONTAINER_PATH, SERVER_DETECT.getWebServerPath());
        detail.put(ReportConstant.COMMON_REMOTE_IP, request.getRemoteAddr());
        detail.put(ReportConstant.COMMON_HTTP_SECURE, request.isSecure());
        detail.put(ReportConstant.COMMON_HTTP_URL, request.getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_URI, request.getRequestURI());
        detail.put(ReportConstant.COMMON_HTTP_METHOD, request.getMethod());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, request.getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Utils.encodeBase64String(request.getHeadersValue().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_CLIENT_IP, request.getRemoteAddr());
        detail.put(ReportConstant.VULN_CALLER, stack);

        EngineManager.sendNewReport(report.toString());
    }

    protected StackTraceElement getLatestStack() {
        return StackUtils.getLatestStack(10);
    }
}
