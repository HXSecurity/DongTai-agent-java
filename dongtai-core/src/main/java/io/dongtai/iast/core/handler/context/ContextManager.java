package io.dongtai.iast.core.handler.context;

/**
 * @author owefsad
 */
public class ContextManager {

    private static ThreadLocal<TracingContext> CONTEXT = new ThreadLocal<TracingContext>();

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

    public static String getSpanId() {
        TracingContext context = getOrCreate();
        return String.valueOf(context.getSpanId());
    }

    public static void addSpanId() {
        TracingContext context = getOrCreate();
        context.setSpanId(context.getSpanId()+1);
    }

    public static String getSegmentId() {
        TracingContext context = CONTEXT.get();
        return context.createSegmentId();
    }

    public static String getHeaderKeyTraceId() {
        return TracingContext.getHeaderKeyTraceId();
    }

}
