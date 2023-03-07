package io.dongtai.iast.core.handler.hookpoint.graphy;

import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.common.utils.base64.Base64Encoder;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.AbstractNormalVulScan;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.StringUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GraphBuilder {

    private static String URL;
    private static String URI;

    public static void buildAndReport() {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            List<GraphNode> nodeList = build();
            String report = convertToReport(nodeList);
            if (report == null) {
                return;
            }
            ThreadPools.sendPriorityReport(ApiPath.REPORT_UPLOAD, report);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.GRAPH_BUILD_AND_REPORT_FAILED, e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    /**
     * 利用污点方法池，构造有序污点调用图 todo 方法内容未开发完成，先跑通流程，再详细补充
     *
     * @return 污点方法列表
     */
    public static List<GraphNode> build() {
        List<GraphNode> nodeList = new ArrayList<GraphNode>();
        Map<Integer, MethodEvent> events = EngineManager.TRACK_MAP.get();

        for (Map.Entry<Integer, MethodEvent> entry : events.entrySet()) {
            nodeList.add(new GraphNode(entry.getValue()));
        }
        return nodeList;
    }

    public static String convertToReport(List<GraphNode> nodeList) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();

        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray methodPool = new JSONArray();

        report.put(ReportKey.TYPE, ReportType.VULN_SAAS_POOL);
        report.put(ReportKey.VERSION, "v3");
        report.put(ReportKey.DETAIL, detail);

        detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportKey.PROTOCOL, requestMeta.getOrDefault("protocol", "unknown"));
        detail.put(ReportKey.SCHEME, requestMeta.getOrDefault("scheme", ""));
        detail.put(ReportKey.METHOD, requestMeta.getOrDefault("method", ""));
        detail.put(ReportKey.SECURE, requestMeta.getOrDefault("secure", false));
        String requestURL = requestMeta.getOrDefault("requestURL", "").toString();
        if (StringUtils.isEmpty(requestURL)) {
            return null;
        }
        detail.put(ReportKey.URL, requestURL);
        String requestURI = requestMeta.getOrDefault("requestURI", "").toString();
        if (StringUtils.isEmpty(requestURI)) {
            return null;
        }

        detail.put(ReportKey.URI, requestURI);
        detail.put(ReportKey.CLIENT_IP, requestMeta.getOrDefault("remoteAddr", ""));
        detail.put(ReportKey.QUERY_STRING, requestMeta.getOrDefault("queryString", ""));
        detail.put(ReportKey.REQ_HEADER, AbstractNormalVulScan.getEncodedHeader(
                (Map<String, String>) requestMeta.getOrDefault("headers", new HashMap<String, String>())));

        String reqBody = (String) requestMeta.get("body");
        if (StringUtils.isEmpty(reqBody)) {
            reqBody = EngineManager.BODY_BUFFER.getRequest().toString();
        }
        detail.put(ReportKey.REQ_BODY, reqBody);

        detail.put(ReportKey.RES_HEADER, AbstractNormalVulScan.getEncodedResponseHeader(
                (String) requestMeta.get("responseStatus"),
                (Map<String, Collection<String>>) requestMeta.get("responseHeaders")));
        String responseBody = EngineManager.BODY_BUFFER.getResponse().toString();
        if (responseBody != null && !responseBody.isEmpty()) {
            responseBody = Base64Encoder.encodeBase64String(responseBody.getBytes(Charset.forName("UTF-8")));
        }
        detail.put(ReportKey.RES_BODY, responseBody);
        detail.put(ReportKey.CONTEXT_PATH, requestMeta.getOrDefault("contextPath", ""));
        detail.put(ReportKey.REPLAY_REQUEST, requestMeta.getOrDefault("replay-request", false));

        detail.put(ReportKey.METHOD_POOL, methodPool);
        detail.put(ReportKey.TRACE_ID, ContextManager.currentTraceId());

        for (GraphNode node : nodeList) {
            methodPool.put(node.toJson());
        }
        return report.toString();
    }
}
