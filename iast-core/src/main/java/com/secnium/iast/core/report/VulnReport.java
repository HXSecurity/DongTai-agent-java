package com.secnium.iast.core.report;

import com.secnium.iast.core.AbstractThread;
import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.base64.Base64Utils;
import com.secnium.iast.core.util.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送报告的功能实现
 *
 * @author dongzhiyong@huoxian.cn
 */
public class VulnReport extends AbstractThread {
    private long waitTime;
    private final Logger logger = LoggerFactory.getLogger(VulnReport.class);
    private static final ServerDetect SERVER_DETECT = ServerDetect.getInstance();

    public VulnReport(long waitTime) {
        super(getThreadName(), true, waitTime);
        this.waitTime = waitTime;
    }

    private static String getThreadName() {
        return "Secnium Agent VulnReport";
    }

    public static String generateVulnReport(String vulType, Object taintValue, HttpRequest request, JSONArray caller) {
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_DYNAMIC);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.AGENT_VERSION, ReportConstant.AGENT_VERSION_VALUE);
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);

        detail.put(ReportConstant.VULN_TYPE, vulType);
        detail.put(ReportConstant.COMMON_APP_NAME, request.getContextPath());
        detail.put(ReportConstant.COMMON_SERVER_NAME, request.getServerName());
        detail.put(ReportConstant.COMMON_SERVER_PORT, request.getServerPort());
        detail.put(ReportConstant.CONTAINER, SERVER_DETECT.getServeName());
        detail.put(ReportConstant.CONTAINER_PATH, SERVER_DETECT.getWebServerPath());

        detail.put(ReportConstant.COMMON_REMOTE_IP, request.getRemoteAddr());
        detail.put(ReportConstant.SERVER_ENV, Base64Utils.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, request.getProtocol());
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, request.getScheme());
        detail.put(ReportConstant.COMMON_HTTP_METHOD, request.getMethod());
        detail.put(ReportConstant.COMMON_HTTP_SECURE, request.isSecure());
        detail.put(ReportConstant.COMMON_HTTP_URL, request.getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_URI, request.getRequestURI());
        detail.put(ReportConstant.COMMON_HTTP_CLIENT_IP, request.getRemoteAddr());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, request.getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Utils.encodeBase64String(request.getHeadersValue().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.VULN_CALLER, caller);
        detail.put(ReportConstant.TAINT_VALUE, taintValue);
        detail.put(ReportConstant.TAINT_POSITION, "HTTP Params");
        detail.put(ReportConstant.TAINT_PARAM_NAME, "");
        return report.toString();
    }

    @Override
    protected void send() throws Exception {
        while (EngineManager.hasNewReport()) {
            String report = EngineManager.getNewReport();
            try {
                if (report != null && !report.isEmpty()) {
                    boolean success = HttpClientUtils.sendPost(Constants.API_REPORT_UPLOAD, report);
                    if (!success) {
                        EngineManager.sendNewReport(report);
                    }
                }
            } catch (Exception e) {
                logger.info(report);
                throw e;
            } finally {
                if (waitTime != PropertyUtils.getInstance().getReportInterval()) {
                    waitTime = PropertyUtils.getInstance().getReportInterval();
                    setMilliseconds();
                }
            }
        }
    }

    public static void main(String[] args) {

    }
}
