package io.dongtai.iast.core.handler.context;

import io.dongtai.iast.core.EngineManager;

/**
 * @author owefsad
 */
public class ContextManager {

    private static ThreadLocal<TracingContext> CONTEXT = new ThreadLocal<TracingContext>();

    public static ThreadLocal<TracingContext> getCONTEXT() {
        return CONTEXT;
    }

    private static TracingContext getOrCreate() {
        TracingContext context = CONTEXT.get();
        if (context == null) {
            context = new TracingContext();
            CONTEXT.set(context);
        }
        return context;
    }

    public static String getOrCreateGlobalTraceId(String traceId, int agentId) {
        TracingContext context = getOrCreate();
        context.parseOrCreateTraceId(traceId, agentId);
        return context.getTraceId();
    }

    public static String getSpanId(String traceId, int agentId) {
        TracingContext context = getOrCreate();
        context.parseOrCreateTraceId(traceId, agentId);
        return String.valueOf(context.getSpanId());
    }

    public static String getSegmentId() {
        TracingContext context = CONTEXT.get();
        if (context != null) {
            return context.createSegmentId();
        }
        return getOrCreateGlobalTraceId(null, EngineManager.getAgentId());
    }

    public static String getHeaderKey() {
        return TracingContext.getHeaderKey();
    }
}
