package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class FeignService {
    public static void solveSyncInvoke(MethodEvent event, AtomicInteger invokeIdSequencer) {
        try {
            if (event.parameterInstances.length != 1) {
                return;
            }

            Object handlerObj = event.objectInstance;
            Field metadataField = handlerObj.getClass().getDeclaredField("metadata");
            metadataField.setAccessible(true);
            Object metadata = metadataField.get(event.objectInstance);
            Method templateMethod = metadata.getClass().getMethod("template");
            Object template = templateMethod.invoke(metadata);

            // get args
            Object args = event.parameterInstances[0];
            TaintPoolUtils.trackObject(event, null, args, 0, true);

            boolean hasTaint = false;
            if (!event.getSourceHashes().isEmpty()) {
                hasTaint = true;
            }
            event.addParameterValue(0, args, hasTaint);

            Method addHeaderMethod = template.getClass().getDeclaredMethod("header", String.class, String[].class);
            addHeaderMethod.setAccessible(true);
            String traceId = ContextManager.nextTraceId();
            // clear old traceId header
            addHeaderMethod.invoke(template, ContextManager.getHeaderKey(), new String[]{});
            addHeaderMethod.invoke(template, ContextManager.getParentKey(), new String[]{});
            addHeaderMethod.invoke(template, ContextManager.getHeaderKey(), new String[]{traceId});
            addHeaderMethod.invoke(template, ContextManager.getParentKey(),
                    new String[]{String.valueOf(EngineManager.getAgentId())});

            // add to method pool
            event.source = false;
            event.traceId = traceId;
            event.setCallStacks(StackUtils.createCallStack(4));
            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            EngineManager.TRACK_MAP.get().put(invokeId, event);
        } catch (NoSuchFieldException ignore) {
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("solve feign invoke failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }
}
