package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.report.ReportThread;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractNormalVulScan implements IVulScan {

    /**
     * @param stacks
     * @param vulType
     */
    public void sendReport(StackTraceElement[] stacks, String vulType) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray vulStacks = new JSONArray();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_NORNAL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.VULN_TYPE, vulType);
        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportConstant.SCHEME, requestMeta.get("scheme"));
        detail.put(ReportConstant.METHOD, requestMeta.get("method"));
        detail.put(ReportConstant.SECURE, requestMeta.get("secure"));
        detail.put(ReportConstant.URL, requestMeta.get("requestURL").toString());
        detail.put(ReportConstant.URI, requestMeta.get("requestURI"));
        detail.put(ReportConstant.CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportConstant.REQ_HEADER, getEncodedHeader((Map<String, String>) requestMeta.get("headers")));
        detail.put(ReportConstant.REQ_BODY, requestMeta.get("body"));
        // fixme add response
        detail.put(ReportConstant.RES_HEADER, "");
        detail.put(ReportConstant.RES_BODY, "");
        detail.put(ReportConstant.CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportConstant.REPLAY_REQUEST, requestMeta.get("replay-request"));
        detail.put(ReportConstant.VULN_CALLER, vulStacks);

        for (StackTraceElement element : stacks) {
            vulStacks.put(element.toString());
        }
        ReportThread.send(Constants.API_REPORT_UPLOAD, report.toString());
    }

    protected StackTraceElement[] getLatestStack() {
        return StackUtils.createCallStack(10);
    }

    public static String getEncodedHeader(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> headerItem : headers.entrySet()) {
            sb.append(headerItem.getKey());
            sb.append(":");
            sb.append(headerItem.getValue());
            sb.append("\n");
        }
        return Base64Encoder.encodeBase64String(sb.toString().getBytes()).replaceAll("\n", "");
    }
}
