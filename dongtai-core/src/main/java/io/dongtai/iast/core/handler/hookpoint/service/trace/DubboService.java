package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
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
            String traceId = ContextManager.nextTraceId();
            //当类型为HessianURLConnection，只处理添加请求头即可
            if (invocation.getClass().getSimpleName().equals("HessianURLConnection")) {
                Method method = invocation.getClass().getMethod("addHeader", String.class, String.class);
                method.setAccessible(true);
                method.invoke(invocation, ContextManager.getHeaderKey(), traceId);
                //因为dubbo已经添加事件，我取出上次事件并对traceId进行修改
                MethodEvent methodEvent = EngineManager.TRACK_MAP.get().get(invokeIdSequencer.get() - 1);
                methodEvent.traceId = traceId;
                return;
            }
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
                setAttachmentMethod.invoke(invocation, ContextManager.getHeaderKey(), traceId);



            // add to method pool
            event.source = false;
            event.traceId = traceId;
            event.setCallStacks(StackUtils.createCallStack(4));
            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setPolicyType(PolicyNodeType.PROPAGATOR.getName());
            EngineManager.TRACK_MAP.get().put(invokeId, event);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("solve dubbo invoke failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }
}
