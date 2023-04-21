package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.common.utils.base64.Base64Encoder;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.vulscan.IVulScan;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.StackUtils;


import java.util.Collection;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractNormalVulScan implements IVulScan {

    /**
     * @param stacks  Method Call Stack
     * @param vulType vulnerability
     */
    public void sendReport(StackTraceElement[] stacks, String vulType) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray vulStacks = new JSONArray();

        report.put(ReportKey.TYPE, ReportType.VULN_NORMAL);
        report.put(ReportKey.DETAIL, detail);

        detail.put("vulnType", vulType);
        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportKey.PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportKey.SCHEME, requestMeta.get("scheme"));
        detail.put(ReportKey.METHOD, requestMeta.get("method"));
        detail.put(ReportKey.SECURE, requestMeta.get("secure"));
        detail.put(ReportKey.URL, requestMeta.get("requestURL").toString());
        detail.put(ReportKey.URI, requestMeta.get("requestURI"));
        detail.put(ReportKey.CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportKey.QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportKey.REQ_HEADER, getEncodedHeader((Map<String, String>) requestMeta.get("headers")));
        detail.put(ReportKey.REQ_BODY, requestMeta.get("body"));
        // fixme add response
        detail.put(ReportKey.RES_HEADER, "");
        detail.put(ReportKey.RES_BODY, "");
        detail.put(ReportKey.CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportKey.REPLAY_REQUEST, requestMeta.get("replay-request"));
        detail.put(ReportKey.VULN_CALLER, vulStacks);

        for (StackTraceElement element : stacks) {
            vulStacks.add(element.toString());
        }
        ThreadPools.sendPriorityReport(ApiPath.REPORT_UPLOAD, report.toString());
    }

    protected StackTraceElement[] getLatestStack() {
        return StackUtils.createCallStack(6);
    }

    public static String getEncodedHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> headerItem : headers.entrySet()) {
            sb.append(headerItem.getKey());
            sb.append(":");
            sb.append(headerItem.getValue());
            sb.append("\n");
        }
        return Base64Encoder.encodeBase64String(sb.toString().getBytes()).replaceAll("\n", "").replaceAll("\r", "");
    }

    public static String getEncodedResponseHeader(String status, Map<String, Collection<String>> headers) {
        if (status == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(status);
        if (headers == null || headers.isEmpty()) {
            return Base64Encoder.encodeBase64String(sb.toString().getBytes()).replaceAll("\n", "").replaceAll("\r", "");
        }
        sb.append("\n");
        for (Map.Entry<String, Collection<String>> headerItem : headers.entrySet()) {
            for (String v : headerItem.getValue()) {
                sb.append(headerItem.getKey());
                sb.append(":");
                sb.append(v);
                sb.append("\n");
            }
        }
        return Base64Encoder.encodeBase64String(sb.toString().getBytes()).replaceAll("\n", "").replaceAll("\r", "");
    }
}
