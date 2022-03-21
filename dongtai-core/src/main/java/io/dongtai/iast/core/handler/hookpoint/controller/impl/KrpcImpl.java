package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KrpcImpl {

    private static final PropertyUtils PROPERTIES = PropertyUtils.getInstance();

    public static void solveKrpc(MethodEvent event, AtomicInteger invokeIdSequencer) {
        // todo handler traceId
        Object invoker = event.argumentArray[0];
        Object invocation = event.argumentArray[1];
        String krpcService = getUrl(invoker);
        Map<String, String> attachments = getAttachments(invocation);
        EngineManager.enterKrpcEntry(krpcService, attachments);

        if (EngineManager.isEnterHttp()) {
            return;
        }

        Object argument = getArguments(invocation);
        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(9));

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.inValue = "";
        //todo: outValue 修改为用户数据相关的内容 或 null
        //event.outValue = argument;

        try {
            handlerCustomModel(event,invocation.getClass().getMethod("asMessage").invoke(invocation));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

    private static String getUrl(Object invocation) {
        try {
            Class<?> RpcMetaClass = invocation.getClass();
            Method connIdMethod = RpcMetaClass.getMethod("getConnId");
            return (String) connIdMethod.invoke(invocation);
        } catch (Exception e) {
            return "";
        }
    }

    private static final String VALUES_ENUMERATOR = " org.apache.tomcat.util.http.ValuesEnumerator".substring(1);
    private static final String SPRING_OBJECT = " org.springframework.".substring(1);

    public static void handlerCustomModel(MethodEvent event,Object argument) {
        try {
            Class<?> sourceClass = argument.getClass();
            if (sourceClass.getClassLoader() == null) {
                return;
            }
            String className = sourceClass.getName();
            if (className.startsWith("cn.huoxian.iast.api.") ||
                    className.startsWith("io.dongtai.api.") ||
                    VALUES_ENUMERATOR.equals(className) ||
                    className.startsWith(SPRING_OBJECT)
            ) {
                return;
            }
            Method[] methods = sourceClass.getDeclaredMethods();
            Object itemValue = null;
            for (Method method : methods) {
                String methodName = method.getName();
                if (!methodName.startsWith("get")
                        || methodName.equals("getClass")
                        || methodName.equals("getParserForType")
                        || methodName.equals("getDefaultInstance")
                        || methodName.equals("getDefaultInstanceForType")
                        || methodName.equals("getDescriptor")
                        || methodName.equals("getDescriptorForType")
                        || methodName.equals("getAllFields")
                        || methodName.equals("getInitializationErrorString")
                        || methodName.equals("getUnknownFields")
                        || methodName.equals("getDetailOrBuilderList")
                        || method.getParameterCount() != 0) {
                    continue;
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == Integer.class ||
                        returnType == Boolean.class ||
                        returnType == Long.class ||
                        returnType == Character.class ||
                        returnType == Double.class ||
                        returnType == Float.class ||
                        returnType == Enum.class ||
                        returnType == Byte.class ||
                        returnType == int.class ||
                        returnType == boolean.class ||
                        returnType == long.class ||
                        returnType == char.class ||
                        returnType == double.class ||
                        returnType == float.class ||
                        returnType == byte.class
                ) {
                    continue;
                }

                itemValue = method.invoke(argument);
                if (!isNotEmpty(itemValue)) {
                    continue;
                }
                addTaintToPool(itemValue, event, true);
            }

        } catch (Throwable t) {
            DongTaiLog.error(t);
        }
    }

    public static void addTaintToPool(Object obj, MethodEvent event, boolean isSource) {
        int subHashCode = 0;
        Set<Object> taintPool = EngineManager.TAINT_POOL.get();
        if (obj instanceof String[]) {
            taintPool.add(obj);
            event.addTargetHash(obj.hashCode());

            String[] tempObjs = (String[]) obj;
            if (PROPERTIES.isNormalMode()) {
                for (String tempObj : tempObjs) {
                    EngineManager.TAINT_POOL.get().add(tempObj);
                    subHashCode = System.identityHashCode(tempObj);
                    EngineManager.TAINT_HASH_CODES.get().add(subHashCode);
                    event.addTargetHash(subHashCode);
                }
            } else {
                for (String tempObj : tempObjs) {
                    EngineManager.TAINT_POOL.get().add(tempObj);
                    event.addTargetHash(tempObj.hashCode());
                }
            }
        } else if (obj instanceof Map) {
            EngineManager.TAINT_POOL.get().add(obj);
            event.addTargetHash(obj.hashCode());
            if (isSource) {
                Map<String, String[]> tempMap = (Map<String, String[]>) obj;
                Set<Map.Entry<String, String[]>> entries = tempMap.entrySet();
                for (Map.Entry<String, String[]> entry : entries) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    addTaintToPool(key, event, true);
                    addTaintToPool(value, event, true);
                }
            }
        } else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
            Object[] tempObjs = (Object[]) obj;
            if (tempObjs.length != 0) {
                for (Object tempObj : tempObjs) {
                    addTaintToPool(tempObj, event, isSource);
                }
            }
        } else {
            taintPool.add(obj);
            Set<Integer> taintHashCodes = EngineManager.TAINT_HASH_CODES.get();
            if (obj instanceof String && PROPERTIES.isNormalMode()) {
                subHashCode = System.identityHashCode(obj);
                taintHashCodes.add(subHashCode);
            } else {
                subHashCode = obj.hashCode();
            }
            event.addTargetHash(subHashCode);

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
            Method methodOfGetAttachments = invocationClass.getMethod("getMeta");
            Object RpcMeta = methodOfGetAttachments.invoke(invocation);
            Class<?> RpcMetaClass = RpcMeta.getClass();
            Map<String, String> RpcMetaMap = new HashMap<>();

            Method getTrace = RpcMetaClass.getMethod("getTrace");
            String traceId = String.valueOf(getTrace.invoke(RpcMeta));
            String[] traceIdSplit = traceId.split("\n");
            for (String s : traceIdSplit) {
                String[] split = s.split(":");
                RpcMetaMap.put(split[0], split[1]);
            }


            return RpcMetaMap;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * get Dubbo Arguments
     *
     * @param invocation object of Invocation
     * @return Object[]
     * @since 1.2.0
     */
    public static Object getArguments(Object invocation) {
        try {
            Class<?> invocationClass = invocation.getClass();
            Method methodOfGetAttachments = invocationClass.getMethod("getBody");
            return methodOfGetAttachments.invoke(invocation);
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
