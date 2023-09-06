package io.dongtai.iast.core.handler.hookpoint.graphy;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.common.utils.base64.Base64Encoder;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.AbstractNormalVulScan;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.common.string.StringUtils;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;


import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class GraphBuilder {

    public static void buildAndReport() {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            String report = convertToReport();
            if (report == null) {
                return;
            }
            ThreadPools.sendPriorityReport(ApiPath.REPORT_UPLOAD, report);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("GRAPH_BUILD_AND_REPORT_FAILED"), e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    public static String convertToReport() {
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
            responseBody = Base64Encoder.encodeBase64String(responseBody.getBytes(StandardCharsets.UTF_8));
        }
        detail.put(ReportKey.RES_BODY, responseBody);
        detail.put(ReportKey.CONTEXT_PATH, requestMeta.getOrDefault("contextPath", ""));
        detail.put(ReportKey.REPLAY_REQUEST, requestMeta.getOrDefault("replay-request", false));

        detail.put(ReportKey.METHOD_POOL, methodPool);
        detail.put(ReportKey.TRACE_ID, ContextManager.currentTraceId());

        Map<Integer, MethodEvent> events = EngineManager.TRACK_MAP.get();
        for (Map.Entry<Integer, MethodEvent> entry : events.entrySet()) {
            MethodEvent event = entry.getValue();
            methodPool.add(toJson(event));
        }

        return report.toString();
    }

    public static JSONObject toJson(MethodEvent event) {
        JSONObject value = new JSONObject();
        JSONArray parameterArray = new JSONArray();
        JSONArray sourceHashArray = new JSONArray();
        JSONArray targetHashArray = new JSONArray();
        JSONObject taintPosition = new JSONObject();
        List<String> sourcePositions = new ArrayList<String>();
        List<String> targetPositions = new ArrayList<String>();

        value.put("invokeId", event.getInvokeId());
        value.put("policyType", event.getPolicyType());
        value.put("source", event.isSource());
        value.put("originClassName", event.getOriginClassName());
        value.put("className", event.getMatchedClassName());
        value.put("methodName", event.getMethodName());
        value.put("signature", event.getSignature());
        value.put("callerClass", event.getCallerClass());
        value.put("callerMethod", event.getCallerMethod());
        value.put("callerLineNumber", event.getCallerLine());
        value.put("sourceHash", sourceHashArray);
        value.put("targetHash", targetHashArray);

        value.put("taintPosition", taintPosition);

        if (event.getSourcePositions() != null && event.getSourcePositions().size() > 0) {
            for (TaintPosition src : event.getSourcePositions()) {
                sourcePositions.add(src.toString());
            }
        }
        if (event.getTargetPositions() != null && event.getTargetPositions().size() > 0) {
            for (TaintPosition tgt : event.getTargetPositions()) {
                targetPositions.add(tgt.toString());
            }
        }
        if (sourcePositions.size() > 0) {
            taintPosition.put("source", sourcePositions);
        }
        if (targetPositions.size() > 0) {
            taintPosition.put("target", targetPositions);
        }

        if (!StringUtils.isEmpty(event.objectValue)) {
            value.put("objValue", event.objectValue);
        } else {
            value.put("objValue", "");
        }
        if (event.parameterValues != null && event.parameterValues.size() > 0) {
            for (MethodEvent.Parameter parameter : event.parameterValues) {
                parameterArray.add(parameter.toJson());
            }
            value.put("parameterValues", parameterArray);
        }
        if (!StringUtils.isEmpty(event.returnValue)) {
            value.put("retValue", event.returnValue);
        }

        sourceHashArray.addAll(event.getSourceHashes());

        targetHashArray.addAll(event.getTargetHashes());

        if (event.targetRanges.size() > 0) {
            JSONArray tr = new JSONArray();
            value.put("targetRange", tr);
            for (MethodEvent.MethodEventTargetRange range : event.targetRanges) {
                tr.add(range.toJson());
            }
        }

        if (event.sourceRanges.size() > 0) {
            JSONArray tr = new JSONArray();
            value.put("sourceRange", tr);
            for (MethodEvent.MethodEventTargetRange range : event.sourceRanges) {
                tr.add(range.toJson());
            }
        }

        if (event.sourceTypes != null && event.sourceTypes.size() > 0) {
            JSONArray st = new JSONArray();
            value.put("sourceType", st);
            for (MethodEvent.MethodEventSourceType s : event.sourceTypes) {
                st.add(s.toJson());
            }
        }

        if (event.traceId != null && !event.traceId.isEmpty()) {
            value.put("traceId", event.traceId);
        }

        if (null != event.getStacks()){
            JSONArray methodStacksArray = new JSONArray(event.getStacks());
            value.put("stacks",methodStacksArray);
        }

        return value;
    }
}
