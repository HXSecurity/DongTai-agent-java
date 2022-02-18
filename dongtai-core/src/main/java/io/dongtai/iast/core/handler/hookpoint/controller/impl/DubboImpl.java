package io.dongtai.iast.core.handler.hookpoint.controller.impl;


import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author owefsad
 * @since 1.2.0
 */
public class DubboImpl {

    public static void solveDubbo(MethodEvent event, AtomicInteger invokeIdSequencer) {
        // todo handler traceId
        Object invoker = event.argumentArray[0];
        Object invocation = event.argumentArray[1];
        String dubboService = getUrl(invoker);
        Map<String, String> attachments = getAttachments(invocation);
        EngineManager.enterDubboEntry(dubboService, attachments);

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

                SourceImpl.handlerCustomModel(event);
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
        } catch (Exception ignored) {
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
}
