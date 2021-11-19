package com.secnium.iast.core.handler.graphy;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.enhance.IastClassAncestorQuery;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GraphBuilder {

    public static void buildAndReport(Object response) {
        List<GraphNode> nodeList = build();
        String report = convertToReport(nodeList, response);
        EngineManager.sendMethodReport(report);
    }

    private static Map<String, Object> getResponseMeta(Object response) {
        Map<String, Object> responseMeta = null;
        try {
            responseMeta = HttpImpl.getResponseMeta(response);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return responseMeta;
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
                            event.getMatchClassName(),
                            event.getOriginClassName(),
                            event.getMethodName(),
                            event.getMethodDesc(),
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

    public static String convertToReport(List<GraphNode> nodeList, Object response) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        Map<String, Object> responseMeta = getResponseMeta(response);
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray methodPool = new JSONArray();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_SAAS_POOL);
        report.put(ReportConstant.REPORT_VALUE_KEY, detail);

        detail.put(ReportConstant.AGENT_ID, EngineManager.getAgentId());
        detail.put(ReportConstant.PROTOCOL, requestMeta.get("protocol"));
        detail.put(ReportConstant.SCHEME, requestMeta.get("scheme"));
        detail.put(ReportConstant.METHOD, requestMeta.get("method"));
        detail.put(ReportConstant.SECURE, requestMeta.get("secure"));
        detail.put(ReportConstant.URL, requestMeta.get("requestURL").toString());
        detail.put(ReportConstant.URI, requestMeta.get("requestURI"));
        detail.put(ReportConstant.CLIENT_IP, requestMeta.get("remoteAddr"));
        detail.put(ReportConstant.QUERY_STRING, requestMeta.get("queryString"));
        detail.put(ReportConstant.REQ_HEADER, Base64Encoder.encodeBase64String(requestMeta.get("headers").toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.REQ_BODY, requestMeta.get("body"));
        detail.put(ReportConstant.RES_HEADER, responseMeta == null ? "" : Base64Encoder.encodeBase64String(responseMeta.get("headers").toString().getBytes()).replaceAll("\n", ""));
        detail.put(ReportConstant.RES_BODY, responseMeta == null ? "" : responseMeta.get("body"));
        detail.put(ReportConstant.CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportConstant.REPLAY_REQUEST, requestMeta.get("replay-request"));

        detail.put(ReportConstant.SAAS_METHOD_POOL, methodPool);

        for (GraphNode node : nodeList) {
            methodPool.put(node.toJson());
        }

        return report.toString();
    }

    private static final Logger logger = LogUtils.getLogger(GraphBuilder.class);
}
