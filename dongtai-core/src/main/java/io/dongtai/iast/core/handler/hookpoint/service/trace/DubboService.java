package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DubboService {
    public static void solveSyncInvoke(MethodEvent event, Object invocation, String url, Map<String, String> headers,
                                       AtomicInteger invokeIdSequencer) {
        try {
            TaintPoolUtils.trackObject(event, null, event.parameterInstances, 0, false);
            boolean hasTaint = false;
            int sourceLen = 0;
            if (!event.getSourceHashes().isEmpty()) {
                hasTaint = true;
                sourceLen = event.getSourceHashes().size();
            }
            event.addParameterValue(0, event.parameterInstances, hasTaint);

            if (headers != null && headers.size() > 0) {
                hasTaint = false;
                TaintPoolUtils.trackObject(event, null, headers, 0, false);
                if (event.getSourceHashes().size() > sourceLen) {
                    hasTaint = true;
                }
                event.addParameterValue(1, headers, hasTaint);
            }

            Method setAttachmentMethod = invocation.getClass().getMethod("setAttachment", String.class, String.class);
            setAttachmentMethod.setAccessible(true);
            String traceId = ContextManager.nextTraceId();
            setAttachmentMethod.invoke(invocation, ContextManager.getHeaderKey(), traceId);

            // add to method pool
            event.source = false;
            event.traceId = traceId;
            event.setCallStacks(StackUtils.createCallStack(4));
            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            EngineManager.TRACK_MAP.get().put(invokeId, event);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("solve dubbo invoke failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }
}
