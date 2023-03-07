package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.common.config.*;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;

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
                put("method", "DUBOO");
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
                                                 Object[] arguments, Map<String, ?> headers,
                                                 String hookClass, String hookMethod, String hookSign,
                                                 AtomicInteger invokeIdSequencer) {
        if (arguments == null || arguments.length == 0) {
            return;
        }
        Map <String, Object> requestMeta = EngineManager.REQUEST_CONTEXT.get();
        if (requestMeta == null) {
            return;
        }

        String url = (String) requestMeta.get("requestURL") + "/" + methodName;
        String uri = (String) requestMeta.get("requestURI") + "/" + methodName;
        requestMeta.put("requestURL", url);
        requestMeta.put("requestURI", uri);

        MethodEvent event = new MethodEvent(hookClass, hookClass, hookMethod,
                hookSign, null, arguments, null);

        HashSet<TaintPosition> src = new HashSet<TaintPosition>();
        HashSet<TaintPosition> tgt = new HashSet<TaintPosition>();
        src.add(new TaintPosition("O"));
        tgt.add(new TaintPosition("P1"));

        SourceNode sourceNode = new SourceNode(src, tgt, null);
        if (arguments != null && arguments.length > 0) {
            TaintPoolUtils.trackObject(event, sourceNode, arguments, 0);
        }

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

        requestMeta.put("headers", sHeaders);
        JSONArray arr = new JSONArray();
        for (Object arg : arguments) {
            arr.put(event.obj2String(arg));
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
        try {
            boolean getBody = ((Config<Boolean>) ConfigBuilder.getInstance().getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
            if (!getBody) {
                return;
            }
        } catch (Throwable ignore) {
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
