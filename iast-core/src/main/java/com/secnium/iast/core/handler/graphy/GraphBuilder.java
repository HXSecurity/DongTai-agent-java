package com.secnium.iast.core.handler.graphy;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.enhance.IastClassAncestorQuery;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GraphBuilder {

    public static void buildAndReport() {
        List<GraphNode> nodeList = build();
        String report = convertToReport(nodeList);
        EngineManager.sendNewReport(report);
    }

    /**
     * 利用污点方法池，构造有序污点调用图
     * todo 方法内容未开发完成，先跑通流程，再详细补充
     *
     * @return 污点方法列表
     */
    public static List<GraphNode> build() {
        PropertyUtils properties = PropertyUtils.getInstance();
        List<GraphNode> nodeList = new ArrayList<GraphNode>();
        Map<Integer, MethodEvent> taintMethodPool = EngineManager.TRACK_MAP.get();

        MethodEvent event = null;
        for (Map.Entry<Integer, MethodEvent> entry : taintMethodPool.entrySet()) {
            event = entry.getValue();
            nodeList.add(
                    new GraphNode(
                            event.isSource(),
                            event.getInvokeId(),
                            event.getCallerClass(),
                            event.getCallerMethod(),
                            event.getCallerLine(),
                            event.object != null ? IastClassAncestorQuery.getFamilyFromClass(event.object.getClass().getName().replace("\\.", "/")) : null,
                            event.getJavaClassName(),
                            event.getJavaMethodName(),
                            event.getJavaMethodDesc(),
                            "",
                            "",
                            event.getSourceHashes(),
                            event.getTargetHashes(),
                            properties.isLocal() ? event.obj2String(event.inValue) : "",
                            properties.isLocal() ? event.obj2String(event.outValue) : ""
                    )
            );
        }
        return nodeList;
    }

    public static String convertToReport(List<GraphNode> nodeList) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray methodPool = new JSONArray();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_SAAS_POOL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        detail.put(ReportConstant.AGENT_ID, AgentRegisterReport.getAgentFlag());
        detail.put(ReportConstant.COMMON_REMOTE_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, requestMeta.get("scheme"));
        detail.put(ReportConstant.COMMON_HTTP_METHOD, requestMeta.get("method"));
        detail.put(ReportConstant.COMMON_HTTP_SECURE, requestMeta.get("secure"));
        detail.put(ReportConstant.COMMON_HTTP_URL, requestMeta.get("requestURL").toString());
        detail.put(ReportConstant.COMMON_HTTP_URI, requestMeta.get("requestURI"));
        detail.put(ReportConstant.COMMON_HTTP_CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Encoder.encodeBase64String(requestMeta.get("headers").toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_BODY, requestMeta.get("body"));
        // fixme 增加响应头/响应体
        detail.put(ReportConstant.COMMON_HTTP_RES_HEADER, "");
        detail.put(ReportConstant.COMMON_HTTP_RES_BODY, "");
        detail.put(ReportConstant.COMMON_HTTP_CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportConstant.COMMON_HTTP_REPLAY_REQUEST, requestMeta.get("replay-request"));

        detail.put(ReportConstant.SAAS_METHOD_POOL, methodPool);

        for (GraphNode node : nodeList) {
            methodPool.put(node.toJson());
        }

        return report.toString();
    }
}
