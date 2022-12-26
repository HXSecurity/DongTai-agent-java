package io.dongtai.iast.core.handler.context;

/**
 * @author owefsad
 */
public class ContextManager {
    private static ThreadLocal<TracingContext> CONTEXT = new ThreadLocal<TracingContext>();

    public static ThreadLocal<TracingContext> getContext() {
        return CONTEXT;
    }

    public static String getHeaderKey() {
        return "dt-traceid";
    }

    public static void parseTraceId(String traceId) {
        TracingContext context = TracingContext.getIncoming(traceId);
        CONTEXT.set(context);
    }

    public static String currentTraceId() {
        TracingContext context = CONTEXT.get();
        if (context == null) {
            context = new TracingContext();
            CONTEXT.set(context);
        }
        return CONTEXT.get().toString();
    }
}
