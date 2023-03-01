package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.*;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * 检测污点池中是否存在目标对象 解决方案， 1.将加入污点池的复杂对象，拆分为简单对象，后续直接检测 2.检测时，将污点池中的复杂对象拆分出来
 * <p>
 * 场景：污点池中数据的查询数多于插入数量
 *
 * @author dongzhiyong@huoxian.cn
 */
public class TaintPoolUtils {
    private static final String VALUES_ENUMERATOR = " org.apache.tomcat.util.http.ValuesEnumerator".substring(1);
    private static final String SPRING_OBJECT = " org.springframework.".substring(1);

    /**
     * 判断 obj 对象是否为 java 的内置数据类型，包括：string、array、list、map、enum 等
     *
     * @param obj Object
     * @return boolean
     */
    public static boolean isJdkType(Object obj) {
        return obj instanceof String || obj instanceof Map || obj instanceof List;
    }

    public static boolean poolContains(Object obj, MethodEvent event) {
        if (obj == null) {
            return false;
        }

        boolean isContains;
        // check object hash exists
        isContains = contains(obj);
        if (isContains) {
            event.addSourceHash(System.identityHashCode(obj));
            return true;
        }

        if (obj instanceof String[]) {
            String[] stringArray = (String[]) obj;
            for (String stringItem : stringArray) {
                if (poolContains(stringItem, event)) {
                    return true;
                }
            }
        } else if (obj instanceof Object[]) {
            Object[] objArray = (Object[]) obj;
            for (Object objItem : objArray) {
                if (poolContains(objItem, event)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断污点是否匹配
     *
     * @param obj Object
     * @return boolean
     */
    private static boolean contains(Object obj) {
        return EngineManager.TAINT_HASH_CODES.contains(System.identityHashCode(obj));
    }

    /**
     * 检查对象是否为空 - 集合类型，检查大小 - 字符串类型，检查是否为空字符串 - 其他情况，均认为非空
     *
     * @param obj 待检查的实例化对象
     * @return true-对象不为空；false-对象为空
     */
    public static boolean isNotEmpty(Object obj) {
        try {
            if (obj == null) {
                return false;
            }
            if (HashCode.calc(obj) == 0) {
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
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean isAllowTaintType(Class<?> objType) {
        return objType != Boolean.class &&
                objType != Boolean[].class &&
                objType != Short.class &&
                objType != Short[].class &&
                objType != Integer.class &&
                objType != Integer[].class &&
                objType != Long.class &&
                objType != Long[].class &&
                objType != Double.class &&
                objType != Double[].class &&
                objType != Float.class &&
                objType != Float[].class &&
                objType != BigDecimal.class &&
                objType != BigDecimal[].class &&
                objType != boolean.class &&
                objType != boolean[].class &&
                objType != short.class &&
                objType != short[].class &&
                objType != int.class &&
                objType != int[].class &&
                objType != long.class &&
                objType != long[].class &&
                objType != double.class &&
                objType != double[].class &&
                objType != float[].class &&
                objType != float.class;
    }

    public static boolean isAllowTaintType(Object obj) {
        if (obj == null) {
            return false;
        }
        return isAllowTaintType(obj.getClass());
    }

    public static Set<Object> parseCustomModel(Object model) {
        Set<Object> modelValues = new HashSet<Object>();
        try {
            if (!TaintPoolUtils.isAllowTaintGetterModel(model)) {
                return modelValues;
            }

            // getter methods
            Method[] methods = model.getClass().getMethods();
            Object itemValue = null;
            for (Method method : methods) {
                if (!TaintPoolUtils.isAllowTaintGetterMethod(method)) {
                    continue;
                }

                try {
                    method.setAccessible(true);
                    itemValue = method.invoke(model);
                    if (!TaintPoolUtils.isNotEmpty(itemValue) || !TaintPoolUtils.isAllowTaintType(itemValue)) {
                        continue;
                    }
                    modelValues.add(itemValue);
                } catch (Throwable e) {
                    DongTaiLog.error(ErrorCode.UTIL_TAINT_PARSE_CUSTOM_MODEL_FAILED,
                            model.getClass().getName(), method.getName(), e);
                }
            }
        } catch (Throwable ignore) {
        }
        return modelValues;
    }

    public static boolean isAllowTaintGetterModel(Object model) {
        if (!TaintPoolUtils.isNotEmpty(model)) {
            return false;
        }
        Class<?> sourceClass = model.getClass();
        if (sourceClass.getClassLoader() == null) {
            return false;
        }
        if (!TaintPoolUtils.isAllowTaintGetterClass(sourceClass)) {
            return false;
        }
        return true;
    }

    public static boolean isAllowTaintGetterClass(Class<?> clazz) {
        String className = clazz.getName();
        if (className.startsWith("cn.huoxian.iast.api.") ||
                className.startsWith("io.dongtai.api.") ||
                className.startsWith(" org.apache.tomcat".substring(1)) ||
                className.startsWith(" org.apache.catalina".substring(1)) ||
                className.startsWith(" org.apache.shiro.web.servlet".substring(1)) ||
                className.startsWith(" org.eclipse.jetty".substring(1)) ||
                VALUES_ENUMERATOR.equals(className) ||
                className.startsWith(SPRING_OBJECT) ||
                className.contains("RequestWrapper") ||
                className.contains("ResponseWrapper")

        ) {
            return false;
        }

        List<Class<?>> interfaces = ReflectUtils.getAllInterfaces(clazz);
        for (Class<?> inter : interfaces) {
            if (inter.getName().endsWith(".servlet.ServletRequest")
                    || inter.getName().endsWith(".servlet.ServletResponse")) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAllowTaintGetterMethod(Method method) {
        String methodName = method.getName();
        if (!methodName.startsWith("get")
                || "getClass".equals(methodName)
                || "getParserForType".equals(methodName)
                || "getDefaultInstance".equals(methodName)
                || "getDefaultInstanceForType".equals(methodName)
                || "getDescriptor".equals(methodName)
                || "getDescriptorForType".equals(methodName)
                || "getAllFields".equals(methodName)
                || "getInitializationErrorString".equals(methodName)
                || "getUnknownFields".equals(methodName)
                || "getDetailOrBuilderList".equals(methodName)
                || "getAllFieldsMutable".equals(methodName)
                || "getAllFieldsRaw".equals(methodName)
                || "getOneofFieldDescriptor".equals(methodName)
                || "getField".equals(methodName)
                || "getFieldRaw".equals(methodName)
                || "getRepeatedFieldCount".equals(methodName)
                || "getRepeatedField".equals(methodName)
                || "getSerializedSize".equals(methodName)
                || "getMethodOrDie".equals(methodName)
                || "getReader".equals(methodName)
                || "getInputStream".equals(methodName)
                || "getWriter".equals(methodName)
                || "getOutputStream".equals(methodName)
                || "getParameterNames".equals(methodName)
                || "getParameterMap".equals(methodName)
                || "getHeaderNames".equals(methodName)
                || methodName.endsWith("Bytes")
                || method.getParameterCount() != 0) {
            return false;
        }

        return isAllowTaintType(method.getReturnType());
    }

    public static void trackObject(MethodEvent event, PolicyNode policyNode, Object obj, int depth) {
        if (depth >= 10 || !TaintPoolUtils.isNotEmpty(obj) || !TaintPoolUtils.isAllowTaintType(obj)) {
            return;
        }

        int hash = 0;
        boolean isSourceNode = policyNode instanceof SourceNode;
        if (isSourceNode) {
            hash = System.identityHashCode(obj);
            if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                return;
            }
        }

        Class<?> cls = obj.getClass();
        if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            trackArray(event, policyNode, obj, depth);
        } else if (obj instanceof Iterator && !(obj instanceof Enumeration)) {
            trackIterator(event, policyNode, (Iterator<?>) obj, depth);
        } else if (obj instanceof Map) {
            trackMap(event, policyNode, (Map<?, ?>) obj, depth);
        } else if (obj instanceof Map.Entry) {
            trackMapEntry(event, policyNode, (Map.Entry<?, ?>) obj, depth);
        } else if (obj instanceof Collection && !(obj instanceof Enumeration)) {
            if (obj instanceof List) {
                trackList(event, policyNode, (List<?>) obj, depth);
            } else {
                trackIterator(event, policyNode, ((Collection<?>) obj).iterator(), depth);
            }
        } else if ("java.util.Optional".equals(obj.getClass().getName())) {
            trackOptional(event, policyNode, obj, depth);
        } else {
            if (isSourceNode) {
                int len = TaintRangesBuilder.getLength(obj);
                if (hash == 0 || len == 0) {
                    return;
                }

                SourceNode sourceNode = (SourceNode) policyNode;
                TaintRanges tr = new TaintRanges(new TaintRange(0, len));
                if (sourceNode.hasTags()) {
                    String[] tags = sourceNode.getTags();
                    for (String tag : tags) {
                        tr.add(new TaintRange(tag, 0, len));
                    }
                }
                event.targetRanges.add(new MethodEvent.MethodEventTargetRange(hash, tr));

                EngineManager.TAINT_HASH_CODES.add(hash);
                event.addTargetHash(hash);
                EngineManager.TAINT_RANGES_POOL.add(hash, tr);
            } else {
                if (!(obj instanceof String)) {
                    Set<Object> modelValues = TaintPoolUtils.parseCustomModel(obj);
                    for (Object modelValue : modelValues) {
                        trackObject(event, policyNode, modelValue, depth + 1);
                    }
                }

                hash = System.identityHashCode(obj);
                if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                    event.addSourceHash(hash);
                }
            }
        }
    }

    private static void trackArray(MethodEvent event, PolicyNode policyNode, Object arr, int depth) {
        int length = Array.getLength(arr);
        for (int i = 0; i < length; i++) {
            trackObject(event, policyNode, Array.get(arr, i), depth + 1);
        }
    }

    private static void trackIterator(MethodEvent event, PolicyNode policyNode, Iterator<?> it, int depth) {
        while (it.hasNext()) {
            trackObject(event, policyNode, it.next(), depth + 1);
        }
    }

    private static void trackMap(MethodEvent event, PolicyNode policyNode, Map<?, ?> map, int depth) {
        for (Object key : map.keySet()) {
            trackObject(event, policyNode, key, depth + 1);
            trackObject(event, policyNode, map.get(key), depth + 1);
        }
    }

    private static void trackMapEntry(MethodEvent event, PolicyNode policyNode, Map.Entry<?, ?> entry, int depth) {
        trackObject(event, policyNode, entry.getKey(), depth + 1);
        trackObject(event, policyNode, entry.getValue(), depth + 1);
    }

    private static void trackList(MethodEvent event, PolicyNode policyNode, List<?> list, int depth) {
        for (Object obj : list) {
            trackObject(event, policyNode, obj, depth + 1);
        }
    }

    private static void trackOptional(MethodEvent event, PolicyNode policyNode, Object obj, int depth) {
        try {
            Object v = ((Optional<?>) obj).orElse(null);
            trackObject(event, policyNode, v, depth + 1);
        } catch (Throwable ignore) {
        }
    }
}
