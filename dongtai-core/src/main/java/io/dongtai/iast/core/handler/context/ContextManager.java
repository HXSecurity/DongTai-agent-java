package io.dongtai.iast.core.handler.context;

/**
 * @author owefsad
 */
public class ContextManager {
    private static final ThreadLocal<TracingContext> CONTEXT = new ThreadLocal<>();

    public static ThreadLocal<TracingContext> getContext() {
        return CONTEXT;
    }

    public static void initContext() {
        TracingContext context = CONTEXT.get();
        if (context == null) {
            context = new TracingContext();
            CONTEXT.set(context);
        }
    }

    public static String getHeaderKey() {
        return "dt-traceid";
    }

    public static String getParentKey() {
        return "dt-parent-agent";
    }

    public static void parseTraceId(String traceId) {
        TracingContext context = TracingContext.getIncoming(traceId);
        CONTEXT.set(context);
    }

    public static String currentTraceId() {
        initContext();
        return CONTEXT.get().toString();
    }

    public static String nextTraceId() {
        initContext();
        return CONTEXT.get().newOutgoing();
    }
}
