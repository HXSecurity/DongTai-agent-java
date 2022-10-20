package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.*;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Array;
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
        if (!TaintPoolUtils.isNotEmpty(event.returnValue)
                || !TaintPoolUtils.isAllowTaintType(event.returnValue)
                || !allowCall(event)) {
            return;
        }

        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(4));

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        // @TODO: use source/target
        event.setInValue(event.argumentArray);
        event.setOutValue(event.returnValue);

        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
        trackTarget(event);
    }

    private static void trackTarget(MethodEvent event) {
        int length = TaintRangesBuilder.getLength(event.returnValue);
        if (length == 0) {
            return;
        }

        trackObject(event, event.returnValue, 0);
        // @TODO: hook json serializer for custom model
        handlerCustomModel(event);
    }

    private static void trackObject(MethodEvent event, Object obj, int depth) {
        if (depth >= 10 || !TaintPoolUtils.isNotEmpty(obj) || !TaintPoolUtils.isAllowTaintType(obj)) {
            return;
        }

        int hash = System.identityHashCode(obj);
        if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
            return;
        }

        Class<?> cls = obj.getClass();
        if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            trackArray(event, obj, depth);
        } else if (obj instanceof Iterator) {
            trackIterator(event, (Iterator<?>) obj, depth);
        } else if (obj instanceof Map) {
            trackMap(event, (Map<?, ?>) obj, depth);
        } else if (obj instanceof Map.Entry) {
            trackMapEntry(event, (Map.Entry<?, ?>) obj, depth);
        } else if (obj instanceof Collection) {
            if (obj instanceof List) {
                trackList(event, (List<?>) obj, depth);
            } else {
                trackIterator(event, ((Collection<?>) obj).iterator(), depth);
            }
        } else if ("java.util.Optional".equals(obj.getClass().getName())) {
            trackOptional(event, obj, depth);
        } else {
            int len = TaintRangesBuilder.getLength(obj);
            if (len == 0) {
                return;
            }

            TaintRanges tr = new TaintRanges(new TaintRange(0, len));
            event.targetRanges.add(new MethodEvent.MethodEventTargetRange(hash, tr));
            EngineManager.TAINT_HASH_CODES.add(hash);
            event.addTargetHash(hash);
            EngineManager.TAINT_RANGES_POOL.add(hash, tr);
        }
    }

    private static void trackArray(MethodEvent event, Object arr, int depth) {
        int length = Array.getLength(arr);
        for (int i = 0; i < length; i++) {
            trackObject(event, Array.get(arr, i), depth);
        }
    }

    private static void trackIterator(MethodEvent event, Iterator<?> it, int depth) {
        while (it.hasNext()) {
            trackObject(event, it.next(), depth + 1);
        }
    }

    private static void trackMap(MethodEvent event, Map<?, ?> map, int depth) {
        for (Object key : map.keySet()) {
            trackObject(event, key, depth);
            trackObject(event, map.get(key), depth);
        }
    }

    private static void trackMapEntry(MethodEvent event, Map.Entry<?, ?> entry, int depth) {
        trackObject(event, entry.getKey(), depth + 1);
        trackObject(event, entry.getValue(), depth + 1);
    }

    private static void trackList(MethodEvent event, List<?> list, int depth) {
        for (Object obj : list) {
            trackObject(event, obj, depth);
        }
    }

    private static void trackOptional(MethodEvent event, Object obj, int depth) {
        try {
            Object v = ((Optional<?>) obj).orElse(null);
            trackObject(event, v, depth);
        } catch (Exception e) {
            DongTaiLog.warn("track optional object failed: " + e.getMessage());
        }
    }

    /**
     * todo: 处理过程和结果需要细化
     *
     * @param event MethodEvent
     */
    public static void handlerCustomModel(MethodEvent event) {
        if (!event.getMethodName().equals("getSession")) {
            Set<Object> modelValues = parseCustomModel(event.returnValue);
            for (Object modelValue : modelValues) {
                trackObject(event, modelValue, 0);
            }
        }
    }

    /**
     * fixme: 解析自定义对象中的可疑数据，当前只解析第一层，可能导致部分变异数据无法跟踪到，不考虑性能的情况下，可疑逐级遍历
     *
     * @param model Object
     * @return Set<Object>
     */
    public static Set<Object> parseCustomModel(Object model) {
        try{
            Set<Object> modelValues = new HashSet<Object>();
            if (!TaintPoolUtils.isNotEmpty(model)) {
                return modelValues;
            }
            Class<?> sourceClass = model.getClass();
            if (sourceClass.getClassLoader() == null) {
                return modelValues;
            }
            String className = sourceClass.getName();
            if (className.startsWith("cn.huoxian.iast.api.") ||
                    className.startsWith("io.dongtai.api.") ||
                    className.startsWith("org.apache.tomcat") ||
                    className.startsWith("org.apache.catalina") ||
                    className.startsWith(" org.apache.shiro.web.servlet".substring(1)) ||
                    VALUES_ENUMERATOR.equals(className) ||
                    className.startsWith(SPRING_OBJECT) ||
                    className.contains("RequestWrapper") ||
                    className.contains("ResponseWrapper")

            ) {
                return modelValues;
            }
            // getter methods
            DongTaiLog.debug(className);
            Method[] methods = sourceClass.getMethods();
            Object itemValue = null;
            for (Method method : methods) {
                if (!TaintPoolUtils.isAllowTaintGetterMethod(method)) {
                    continue;
                }

                try {
                    itemValue = method.invoke(model);
                    if (!TaintPoolUtils.isNotEmpty(itemValue)) {
                        continue;
                    }
                    modelValues.add(itemValue);
                    if (itemValue instanceof List) {
                        List<?> itemValueList = (List<?>) itemValue;
                        for (Object listValue : itemValueList) {
                            modelValues.addAll(parseCustomModel(listValue));
                        }
                    }
                } catch (Exception e) {
                    DongTaiLog.error(e);
                }
            }
            return modelValues;
        }catch (Exception e){
            return new HashSet<Object>();
        }
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

    static {
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.bestMatchingPattern".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.uriTemplateVariables".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.View.pathVariables".substring(1));
    }

}
