package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class FeignService {

    public static void solveSyncInvoke(MethodEvent event, AtomicInteger invokeIdSequencer) {
        try {
            Object parameterInstance = event.parameterInstances[0];

            Method appendHeader = parameterInstance.getClass().getDeclaredMethod("appendHeader", String.class, String[].class);
            appendHeader.setAccessible(true);


            // get args
            Object args = event.parameterInstances[0];
            TaintPoolUtils.trackObject(event, null, args, 0, true);

            boolean hasTaint = !event.getSourceHashes().isEmpty();
            event.addParameterValue(0, args, hasTaint);

            // clear old traceId header
            //曾经的hook点在高并发下有线程安全问题，新hook点判断无此问题，除去锁
            String traceId = ContextManager.nextTraceId();
            DongTaiLog.trace("InvokeId is {}, request headers is {}",event.getInvokeId(),traceId);
            appendHeader.invoke(parameterInstance,ContextManager.getHeaderKey(),new String[]{});
            appendHeader.invoke(parameterInstance,ContextManager.getParentKey(),new String[]{});
            appendHeader.invoke(parameterInstance,ContextManager.getHeaderKey(),new String[]{traceId});
            appendHeader.invoke(parameterInstance,ContextManager.getParentKey(),new String[]{String.valueOf(EngineManager.getAgentId())});
            event.traceId = traceId;

            // add to method pool
            event.source = false;
            event.setCallStacks(StackUtils.createCallStack(4));
            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setPolicyType(PolicyNodeType.PROPAGATOR.getName());
            EngineManager.TRACK_MAP.get().put(invokeId, event);
        } catch (NoSuchMethodException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("solve feign invoke failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }

}
