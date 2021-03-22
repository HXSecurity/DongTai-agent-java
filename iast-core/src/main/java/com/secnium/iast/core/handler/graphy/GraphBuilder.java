package com.secnium.iast.core.handler.graphy;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.enhance.IASTClassAncestorQuery;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.util.base64.Base64Utils;
import com.secnium.iast.core.util.http.HttpRequest;
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
                            event.object != null ? IASTClassAncestorQuery.getFamilyFromClass(event.object.getClass().getName().replace("\\.", "/")) : null,
                            event.getJavaClassName(),
                            event.getJavaMethodName(),
                            event.getJavaMethodDesc(),
                            "",
                            "",
                            event.getSourceHashes(),
                            event.getTargetHashes()
                    )
            );
        }
        return nodeList;
    }

    public static String convertToReport(List<GraphNode> nodeList) {
        HttpRequest request = EngineManager.REQUEST_CONTEXT.get();
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray methodPool = new JSONArray();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_SAAS_POOL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_NAME, AgentRegisterReport.getAgentToken());
        detail.put(ReportConstant.COMMON_REMOTE_IP, request.getRemoteAddr());
        detail.put(ReportConstant.COMMON_HTTP_PROTOCOL, request.getProtocol());
        detail.put(ReportConstant.COMMON_HTTP_SCHEME, request.getScheme());
        detail.put(ReportConstant.COMMON_HTTP_METHOD, request.getMethod());
        detail.put(ReportConstant.COMMON_HTTP_SECURE, request.isSecure());
        detail.put(ReportConstant.COMMON_HTTP_URL, request.getRequestURL());
        detail.put(ReportConstant.COMMON_HTTP_URI, request.getRequestURI());
        detail.put(ReportConstant.COMMON_HTTP_CLIENT_IP, request.getRemoteAddr());
        detail.put(ReportConstant.COMMON_HTTP_QUERY_STRING, request.getQueryString());
        detail.put(ReportConstant.COMMON_HTTP_REQ_HEADER, Base64Utils.encodeBase64String(request.getHeadersValue().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.COMMON_HTTP_CONTEXT_PATH, request.getContextPath());

        detail.put(ReportConstant.SAAS_METHOD_POOL, methodPool);

        for (GraphNode node : nodeList) {
            methodPool.put(node.toJson());
        }

        return report.toString();
    }

    // a,b,c,d,e,f,g
    // 栈
    // a
    // ab
    // abc
    // abd
    // abd

}
