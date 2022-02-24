package io.dongtai.iast.core.handler.hookpoint.graphy;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.bytecode.enhance.IastClassDiagram;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.AbstractNormalVulScan;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.Constants;
import io.dongtai.iast.core.utils.base64.Base64Encoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GraphBuilder {

    public static void buildAndReport(Object request, Object response) {
        List<GraphNode> nodeList = build();
        String report = convertToReport(nodeList, request, response);
        ThreadPools.sendPriorityReport(Constants.API_REPORT_UPLOAD, report);
    }

    /**
     * 利用污点方法池，构造有序污点调用图 todo 方法内容未开发完成，先跑通流程，再详细补充
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
                            event.object != null ? IastClassDiagram
                                    .getFamilyFromClass(event.object.getClass().getName().replace("\\.", "/")) : null,
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

    public static String convertToReport(List<GraphNode> nodeList, Object request, Object response) {
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        Map<String, Object> responseMeta = response == null ? null : HttpImpl.getResponseMeta(response);
        JSONObject report = new JSONObject();
        JSONObject detail = new JSONObject();
        JSONArray methodPool = new JSONArray();

        report.put(ReportConstant.REPORT_KEY, ReportConstant.REPORT_VULN_SAAS_POOL);
        report.put(ReportConstant.REPORT_TYPE, "v2");
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
        detail.put(ReportConstant.REQ_HEADER,
                AbstractNormalVulScan.getEncodedHeader((Map<String, String>) requestMeta.get("headers")));
        // 设置请求体
        detail.put(ReportConstant.REQ_BODY, request == null ? "" : HttpImpl.getPostBody(request));
        detail.put(ReportConstant.RES_HEADER, responseMeta == null ? ""
                : Base64Encoder.encodeBase64String(responseMeta.get("headers").toString().getBytes())
                .replaceAll("\n", ""));
        detail.put(ReportConstant.RES_BODY, responseMeta == null ? "" : Base64Encoder.encodeBase64String(
                getResponseBody(responseMeta)));
        detail.put(ReportConstant.CONTEXT_PATH, requestMeta.get("contextPath"));
        detail.put(ReportConstant.REPLAY_REQUEST, requestMeta.get("replay-request"));

        detail.put(ReportConstant.SAAS_METHOD_POOL, methodPool);

        for (GraphNode node : nodeList) {
            methodPool.put(node.toJson());
        }

        return report.toString();
    }

    private static byte[] getResponseBody(Map<String, Object> responseMeta) {
        Integer responseLength = PropertyUtils.getInstance().getResponseLength();
        byte[] responseBody = (byte[]) responseMeta.get("body");
        if (responseLength > 0) {
            byte[] newResponseBody = new byte[responseLength];
            newResponseBody = Arrays.copyOfRange(responseBody, 0, responseLength);
            return newResponseBody;
        } else if (responseLength == 0) {
            return new byte[0];
        } else {
            return responseBody;
        }
    }
}
