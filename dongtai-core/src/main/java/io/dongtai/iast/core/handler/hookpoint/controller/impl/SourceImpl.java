package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.StackUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 污点来源处理方法
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SourceImpl {

    /**
     * 属性黑名单，用于检测属性是否可用
     */
    private static final ArrayList<String> WHITE_ATTRIBUTES = new ArrayList<String>();
    private static final String METHOD_OF_GETATTRIBUTE = "getAttribute";
    private static final String VALUES_ENUMERATOR = " org.apache.tomcat.util.http.ValuesEnumerator".substring(1);
    private static final String SPRING_OBJECT = " org.springframework.".substring(1);

    public static void solveSource(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (isNotEmpty(event.returnValue) && isAllowTaintType(event.returnValue) && allowCall(event)) {
            event.source = true;
            event.setCallStacks(StackUtils.createCallStack(9));

            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            event.inValue = event.argumentArray;
            event.outValue = event.returnValue;

            if (isNotEmpty(event.returnValue)) {
                handlerCustomModel(event);
                EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
                EngineManager.TAINT_POOL.addTaintToPool(event.returnValue, event, true);
            }
        }
    }

    /**
     * todo: 处理过程和结果需要细化
     *
     * @param event
     */
    public static void handlerCustomModel(MethodEvent event) {
        Set<Object> modelValues = parseCustomModel(event.returnValue);
        for (Object modelValue : modelValues) {
            EngineManager.TAINT_POOL.addTaintToPool(modelValue, event, true);
        }
    }

    /**
     * fixme: 解析自定义对象中的可疑数据，当前只解析第一层，可能导致部分变异数据无法跟踪到，不考虑性能的情况下，可疑逐级遍历
     *
     * @param model
     * @return
     */
    public static Set<Object> parseCustomModel(Object model) {
        Set<Object> modelValues = new HashSet<Object>();
        Class<?> sourceClass = model.getClass();
        if (sourceClass.getClassLoader() == null) {
            return modelValues;
        }
        String className = sourceClass.getName();
        if (className.startsWith("cn.huoxian.iast.api.") ||
                className.startsWith("io.dongtai.api.") ||
                VALUES_ENUMERATOR.equals(className) ||
                className.startsWith(SPRING_OBJECT)
        ) {
            return modelValues;
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
                    || methodName.equals("getAllFieldsMutable")
                    || methodName.equals("getAllFieldsRaw")
                    || methodName.equals("getOneofFieldDescriptor")
                    || methodName.equals("getField")
                    || methodName.equals("getFieldRaw")
                    || methodName.equals("getRepeatedFieldCount")
                    || methodName.equals("getRepeatedField")
                    || methodName.equals("getSerializedSize")
                    || methodName.equals("getMethodOrDie")
                    || methodName.endsWith("Bytes")
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

            try {
                itemValue = method.invoke(model);
                if (!isNotEmpty(itemValue)) {
                    continue;
                }
                modelValues.add(itemValue);
                if (itemValue instanceof List) {
                    List<?> itemValueList = (List<?>) itemValue;
                    for (Object listValue : itemValueList) {
                        modelValues.addAll(parseCustomModel(listValue));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return modelValues;
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

    private static boolean allowCall(MethodEvent event) {
        boolean allowed = true;
        if (METHOD_OF_GETATTRIBUTE.equals(event.getMethodName())) {
            return allowAttribute((String) event.argumentArray[0]);
        }
        return allowed;
    }

    /**
     * 检查属性是否xxx，时间复杂度：O(n) fixme: spring参数解析，白名单导致数据不正确
     *
     * @param attribute 属性名称
     * @return true-属性允许，false-属性不允许
     */
    private static boolean allowAttribute(String attribute) {
        return WHITE_ATTRIBUTES.contains(attribute);
    }


    public static boolean isAllowTaintType(Object obj) {
        return !(obj instanceof Boolean || obj instanceof Integer);
    }

    static {
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.bestMatchingPattern".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.uriTemplateVariables".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.View.pathVariables".substring(1));
    }

}
