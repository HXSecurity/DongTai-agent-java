package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractNormalVulScan implements IVulScan {

    /**
     * @param stack
     * @param vulType
     */
    public void sendReport(StackTraceElement stack, String vulType) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.SERVER_NAME, null != EngineManager.SERVER ? EngineManager.SERVER.getServerAddr() : "");
        detail.put(ReportConstant.SERVER_PORT, null != EngineManager.SERVER ? EngineManager.SERVER.getServerPort() : "");
        detail.put(ReportConstant.SERVER_ENV, Base64Encoder.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.HOSTNAME, getInternalHostName());
        detail.put(ReportConstant.AGENT_REPORT_VERSION, ReportConstant.AGENT_VERSION_VALUE);
        detail.put(ReportConstant.APP_NAME, PropertyUtils.getInstance().getProjectName());
        detail.put(ReportConstant.APP_PATH, requestMeta.get("contextPath"));
        // fixme taintValue taintPosition paramName container
        detail.put(ReportConstant.VULN_TYPE, vulType);
        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.HTTP_PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportConstant.HTTP_SCHEME, requestMeta.get("scheme"));
        detail.put(ReportConstant.HTTP_METHOD, requestMeta.get("method"));
        detail.put(ReportConstant.HTTP_SECURE, requestMeta.get("secure"));
        detail.put(ReportConstant.HTTP_URL, requestMeta.get("requestURL").toString());
        detail.put(ReportConstant.HTTP_URI, requestMeta.get("requestURI"));
        detail.put(ReportConstant.HTTP_CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.HTTP_QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportConstant.HTTP_REQ_HEADER, Base64Encoder.encodeBase64String(requestMeta.get("headers").toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.HTTP_BODY, requestMeta.get("body"));
        // fixme add response
        detail.put(ReportConstant.HTTP_RES_HEADER, "");
        detail.put(ReportConstant.HTTP_RES_BODY, "");
        detail.put(ReportConstant.CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportConstant.HTTP_REPLAY_REQUEST, requestMeta.get("replay-request"));
        detail.put(ReportConstant.VULN_CALLER, stack);

        EngineManager.sendNewReport(report.toString());
    }

    protected StackTraceElement getLatestStack() {
        return StackUtils.getLatestStack(10);
    }

    private String getInternalHostName() {
        if (System.getenv("COMPUTERNAME") != null) {
            return System.getenv("COMPUTERNAME");
        } else {
            return getHostNameForLinux();
        }
    }

    private String getHostNameForLinux() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }
}
