package com.secnium.iast.core.report;

import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.base64.Base64Utils;
import com.secnium.iast.core.util.http.HttpRequest;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class OverPowerReport {

    public static String geneate(HttpRequest request, Object sql, String authInfo, String traceId) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_OVER_POWER);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.OVER_POWER_SQL, sql);
        detail.put(ReportConstant.OVER_POWER_AUTH_COOKIE, authInfo);
        detail.put(ReportConstant.OVER_POWER_TRACE_ID, traceId);
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, request.getScheme());
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, request.getProtocol());
        detail.put(ReportConstant.COMMON_HTTP_METHOD, request.getMethod());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, request.getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Utils.encodeBase64String(request.getHeadersValue().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_URI, request.getRequestURI());
        detail.put(ReportConstant.COMMON_HTTP_URL, request.getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_BODY, request.getCachedBody());
        detail.put(ReportConstant.COMMON_APP_NAME, request.getContextPath());
        detail.put(ReportConstant.COMMON_SERVER_NAME, request.getServerName());
        detail.put(ReportConstant.COMMON_SERVER_PORT, request.getServerPort());
        detail.put(ReportConstant.COMMON_REMOTE_IP, request.getRemoteAddr());

        return report.toString();
    }
}
