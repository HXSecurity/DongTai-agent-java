package io.dongtai.iast.core.handler.hookpoint.framework.dubbo;


import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastServer;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.SourceImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author owefsad
 * @since 1.2.0
 */
public class DubboHandler {
    private static ThreadLocal<String> sharedTraceId = new ThreadLocal<String>();

    /**
     * @param dubboService
     * @param attachments
     * @since 1.2.0
     */
    public static void enterDubboEntry(String dubboService, Map<String, String> attachments) {
        if (attachments != null) {
            if (attachments.containsKey(ContextManager.getHeaderKey())) {
                ContextManager.getOrCreateGlobalTraceId(attachments.get(ContextManager.getHeaderKey()),
                        EngineManager.getAgentId());
            } else {
                String segmentId = ContextManager.getSegmentId();
                attachments.put(ContextManager.getHeaderKey(), segmentId);
                sharedTraceId.set(segmentId);
            }
        }
        if (EngineManager.isEnterEntry("DUBBO")) {
            return;
        }

        // todo: register server
        if (attachments != null) {
            Map<String, String> requestHeaders = new HashMap<String, String>(attachments.size());
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                requestHeaders.put(entry.getKey(), entry.getValue());
            }
            if (null == EngineManager.SERVER) {
                // todo: read server addr and send to OpenAPI Service
                EngineManager.SERVER = new IastServer(requestHeaders.get("dubbo"), 0, true);
            }
            Map<String, Object> requestMeta = new HashMap<String, Object>(12);
            requestMeta.put("protocol", "dubbo/" + requestHeaders.get("dubbo"));
            requestMeta.put("scheme", "dubbo");
            requestMeta.put("method", "RPC");
            requestMeta.put("secure", "true");
            requestMeta.put("requestURL", dubboService.split("\\?")[0]);
            requestMeta.put("requestURI", requestHeaders.get("path"));
            requestMeta.put("remoteAddr", "");
            requestMeta.put("queryString", "");
            requestMeta.put("headers", requestHeaders);
            requestMeta.put("body", "");
            requestMeta.put("contextPath", "");
            requestMeta.put("replay-request", false);

            EngineManager.REQUEST_CONTEXT.set(requestMeta);
        }

        EngineManager.TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        EngineManager.TAINT_POOL.set(new HashSet<Object>());
        EngineManager.TAINT_HASH_CODES.set(new HashSet<Integer>());
    }

    public static void solveDubbo(MethodEvent event, AtomicInteger invokeIdSequencer) {
        // todo handler traceId
        Object invoker = event.argumentArray[0];
        Object invocation = event.argumentArray[1];
        String dubboService = getUrl(invoker);
        Map<String, String> attachments = getAttachments(invocation);
        enterDubboEntry(dubboService, attachments);

        if (EngineManager.isEnterHttp()) {
            return;
        }

        Object[] arguments = getArguments(invocation);
        if (arguments != null && arguments.length > 0) {
            Set<Object> validArguments = new HashSet<Object>(arguments.length);
            for (Object argument : arguments) {
                if (isNotEmpty(argument) && isAllowTaintType(argument)) {
                    validArguments.add(argument);
                }
            }
            if (!validArguments.isEmpty()) {
                Object[] verifiedArguments = validArguments.toArray();
                event.source = true;
                event.setCallStacks(StackUtils.createCallStack(9));

                int invokeId = invokeIdSequencer.getAndIncrement();
                event.setInvokeId(invokeId);
                event.inValue = "";
                event.outValue = verifiedArguments;

                EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
                EngineManager.TAINT_POOL.addTaintToPool(verifiedArguments, event, true);
            }
        }
    }

    /**
     * @param invoker Object of Invoker
     * @return dubbo service full str, eg: dubbo://192.168.1.104:20880/org.apache.skywalking.demo.interfaces.HelloService?anyhost=true&application=dubbo-provider&bind.ip=192.168.1.104&bind.port=20880&dubbo=2.6.2&generic=false&interface=org.apache.skywalking.demo.interfaces.HelloService&methods=sayHello&pid=22816&revision=1.0.0&side=provider&timeout=60000&timestamp=1639933330390&version=1.0.0
     * @since 1.2.0
     */
    public static String getUrl(Object invoker) {
        try {
            Class<?> invokerClass = invoker.getClass();
            Method methodOfGetUrl = invokerClass.getMethod("getUrl");
            methodOfGetUrl.setAccessible(true);
            return methodOfGetUrl.invoke(invoker).toString();
        } catch (Exception e) {
            DongTaiLog.debug(e);
            return null;
        }
    }

    /**
     * get Dubbo Attachments
     *
     * @param invocation object of Invocation
     * @return Map<String, String>
     * @since 1.2.0
     */
    public static Map<String, String> getAttachments(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getAttachments");
            return (Map<String, String>) methodOfGetAttachments.invoke(invocation);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get Dubbo Arguments
     *
     * @param invocation object of Invocation
     * @return Object[]
     * @since 1.2.0
     */
    public static Object[] getArguments(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getArguments");
            return (Object[]) methodOfGetAttachments.invoke(invocation);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAllowTaintType(Object obj) {
        return !(obj instanceof Boolean || obj instanceof Integer);
    }

    /**
     * 检查对象是否为空 - 集合类型，检查大小 - 字符串类型，检查是否为空字符串 - 其他情况，均认为非空
     *
     * @param obj 待检查的实例化对象
     * @return true-对象不为空；false-对象为空
     */
    private static boolean isNotEmpty(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Map) {
            Map<?, ?> taintValue = (Map<?, ?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof List) {
            List<?> taintValue = (List<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof Set) {
            Set<?> taintValue = (Set<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof String) {
            String taintValue = (String) obj;
            return !taintValue.isEmpty();
        }
        return true;
    }

    public static void solveClientExit(Object invocation, Object rpcResult) {
        System.out.println("");
        if (EngineManager.TAINT_POOL.get().isEmpty()) {
            return;
        }

        Object[] args = getArguments(invocation);
        if (args == null || args.length == 0) {
            return;
        }

        MethodEvent event = new MethodEvent(
                0,
                0,
                "*.dubbo.monitor.support.MonitorFilter",
                "*.dubbo.monitor.support.MonitorFilter",
                "invoke",
                "com.alibaba.dubbo.monitor.support.MonitorFilter#invoke",
                "com.alibaba.dubbo.monitor.support.MonitorFilter#invoke",
                null,
                new Object[]{invocation},
                rpcResult,
                "DUBBO",
                false,
                null
        );
        //SourceImpl.parseCustomModel(args)
        Set<Object> modelItems = new HashSet<>();
        for (Object arg : args) {
            if (TaintPoolUtils.isJdkType(arg)) {
                modelItems.add(arg);
            } else {
                modelItems.add(SourceImpl.parseCustomModel(arg));
            }
        }
        boolean isHitTaints = false;
        for (Object item : modelItems) {
            isHitTaints = isHitTaints || TaintPoolUtils.poolContains(item, event, true);
        }
        if (isHitTaints) {
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setPlugin("DUBBO");
            // todo: 获取 service name
            event.setServiceName("");
            // todo: 获取 traceId
            event.setTraceId(sharedTraceId.get());
            event.setCallStack(StackUtils.getLatestStack(5));
            EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
            Set<Object> resModelItems = SourceImpl.parseCustomModel(rpcResult);
            Set<Object> taintPool = EngineManager.TAINT_POOL.get();
            Set<Object> resModelSet = new HashSet<Object>();
            for (Object obj : resModelItems) {
                // fixme: 暂时只跟踪字符串相关内容
                if (obj instanceof String) {
                    resModelSet.add(obj);
                    taintPool.add(obj);
                    int identityHashCode = System.identityHashCode(obj);
                    event.addTargetHash(identityHashCode);
                    event.addTargetHashForRpc(obj.hashCode());
                    EngineManager.TAINT_HASH_CODES.get().add(identityHashCode);
                }
            }
            event.outValue = resModelSet;
        }
    }

    public static void solveServiceExit(Object invocation, Object rpcResult) {
        if (!EngineManager.TAINT_POOL.get().isEmpty()) {
            MethodEvent event = new MethodEvent(
                    0,
                    0,
                    "*.dubbo.monitor.support.MonitorFilter",
                    "*.dubbo.monitor.support.MonitorFilter",
                    "invoke",
                    "com.alibaba.dubbo.monitor.support.MonitorFilter#invoke",
                    "com.alibaba.dubbo.monitor.support.MonitorFilter#invoke",
                    null,
                    new Object[]{rpcResult},
                    null,
                    "DUBBO",
                    false,
                    null
            );
            Set<Object> modelItems = SourceImpl.parseCustomModel(rpcResult);
            boolean isHitTaints = false;
            for (Object item : modelItems) {
                isHitTaints = isHitTaints || TaintPoolUtils.poolContains(item, event, false);
            }
            if (isHitTaints) {
                int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
                event.setInvokeId(invokeId);
                event.setPlugin("DUBBO");
                event.setServiceName("");
                event.setProjectPropagatorClose(true);
                event.setCallStack(StackUtils.getLatestStack(5));
                EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
            }
        }
    }
}
