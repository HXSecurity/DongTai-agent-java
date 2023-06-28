package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import com.alibaba.fastjson2.JSONArray;
import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.config.ConfigKey;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.*;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DubboImpl {
    public static void solveDubboRequest(Object handler, Object channel, Object request, String url, String remoteAddress) {
        try {
            URI u = new URI(url);
            Map<String, Object> requestMeta = new HashMap<String, Object>() {{
                put("requestURL", u.getScheme() + "://" + u.getAuthority() + u.getPath());
                put("requestURI", u.getPath());
                put("queryString", "");
                put("method", "DUBBO");
                put("protocol", "DUBBO");
                put("scheme", u.getScheme());
                put("contextPath", "");
                put("remoteAddr", "0:0:0:0:0:0:0:1".equals(remoteAddress) ? "127.0.0.1" : remoteAddress);
                put("secure", false);
                put("serverPort", u.getPort());
            }};

            EngineManager.enterDubboEntry(requestMeta);
            DongTaiLog.debug("Dubbo: {}", request.toString());
        } catch (URISyntaxException ignore) {
        }
    }


    public static void collectDubboRequestSource(Object handler, Object invocation, String methodName,
                                                 Object[] arguments, Class<?>[] argumentTypes, Map<String, ?> headers,
                                                 String hookClass, String hookMethod, String hookSign,
                                                 AtomicInteger invokeIdSequencer) {
        if (arguments == null || arguments.length == 0) {
            return;
        }
        Map<String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        if (requestMeta == null) {
            return;
        }

        String url = (String) requestMeta.get("requestURL") + "/" + methodName;
        String uri = (String) requestMeta.get("requestURI") + "/" + methodName;

        StringBuilder argSign = new StringBuilder("(");
        if (argumentTypes != null && argumentTypes.length > 0) {
            int i = 0;
            for (Class<?> argumentType : argumentTypes) {
                if (i != 0) {
                    argSign.append(",");
                }
                argSign.append(argumentType.getCanonicalName());
                i++;
            }
        }
        argSign.append(")");
        String argSignStr = argSign.toString();
        url += argSignStr;
        uri += argSignStr;

        requestMeta.put("requestURL", url);
        requestMeta.put("requestURI", uri);

        MethodEvent event = new MethodEvent(hookClass, hookClass, hookMethod,
                hookSign, null, arguments, null);

        HashSet<TaintPosition> src = new HashSet<TaintPosition>();
        HashSet<TaintPosition> tgt = new HashSet<TaintPosition>();
        src.add(new TaintPosition("O"));
        tgt.add(new TaintPosition("P1"));

        SourceNode sourceNode = new SourceNode(src, tgt, null);
        TaintPoolUtils.trackObject(event, sourceNode, arguments, 0, true);

        Map<String, String> sHeaders = new HashMap<String, String>();
        if (headers != null) {
            for (Map.Entry<String, ?> entry : headers.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                sHeaders.put(entry.getKey(), entry.getValue().toString());
            }
        }

        if (!sHeaders.isEmpty()) {
            String traceIdKey = ContextManager.getHeaderKey();
            if (headers.containsKey(traceIdKey)) {
                ContextManager.parseTraceId(sHeaders.get(traceIdKey));
            } else {
                String newTraceId = ContextManager.currentTraceId();
                sHeaders.put(traceIdKey, newTraceId);
            }
        }

        if (event.getTargetHashes().isEmpty()) {
            return;
        }

        event.addParameterValue(0, arguments, true);
        event.setObjectValue(handler, false);
        event.setTaintPositions(sourceNode.getSources(), sourceNode.getTargets());

        // for display taint range (full arguments value)
        String fv = event.parameterValues.get(0).getValue();
        long hash = TaintPoolUtils.toStringHash(fv.hashCode(),System.identityHashCode(fv));
        int len = TaintRangesBuilder.getLength(fv);
        TaintRanges tr = new TaintRanges(new TaintRange(0, len));
        event.targetRanges.add(0, new MethodEvent.MethodEventTargetRange(hash, tr));

        requestMeta.put("headers", sHeaders);
        JSONArray arr = new JSONArray();
        for (Object arg : arguments) {
            arr.add(event.obj2String(arg));
        }
        requestMeta.put("body", arr.toString());
        EngineManager.REQUEST_CONTEXT.set(requestMeta);

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);

        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(4));

        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

    public static void collectDubboResponse(Object result, byte status) {
        if (result == null) {
            return;
        }

        Boolean getBody = ConfigBuilder.getInstance().get(ConfigKey.REPORT_RESPONSE_BODY);
        // default true
        if (getBody != null && !getBody) {
            return;
        }

        try {
            EngineManager.REQUEST_CONTEXT.get().put("responseStatus",
                    (String) EngineManager.REQUEST_CONTEXT.get().get("protocol") + " " + status);
            ByteArrayOutputStream buff = EngineManager.BODY_BUFFER.getResponse();
            buff.write(result.toString().getBytes());
        } catch (Throwable ignore) {
        }
    }
}
