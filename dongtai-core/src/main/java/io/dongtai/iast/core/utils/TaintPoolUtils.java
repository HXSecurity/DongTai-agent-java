package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.*;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    public static boolean poolContains(Object obj, MethodEvent event) {
        if (obj == null) {
            return false;
        }

        long hash = getStringHash(obj);
        boolean isContains;
        // check object hash exists
        isContains = contains(hash);
        if (isContains) {
            event.addSourceHash(hash);
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
     * @param hash long
     * @return boolean
     */
    private static boolean contains(long hash) {
        return EngineManager.TAINT_HASH_CODES.contains(hash);
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

    public static void trackObject(MethodEvent event, PolicyNode policyNode, Object obj, int depth, Boolean isMicroservice) {
        if (depth >= 10 || !TaintPoolUtils.isNotEmpty(obj) || !TaintPoolUtils.isAllowTaintType(obj)) {
            return;
        }

        long hash = 0;
        long identityHash = 0;
        boolean isSourceNode = policyNode instanceof SourceNode;
        if (isSourceNode) {
            if (obj instanceof String) {
                identityHash = System.identityHashCode(obj);
                hash = toStringHash(obj.hashCode(), identityHash);
            } else {
                hash = System.identityHashCode(obj);
                identityHash = hash;
            }
            if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                return;
            }
        }

        Class<?> cls = obj.getClass();
        if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            trackArray(event, policyNode, obj, depth, isMicroservice);
        } else if (obj instanceof Iterator && !(obj instanceof Enumeration)) {
            trackIterator(event, policyNode, (Iterator<?>) obj, depth, isMicroservice);
        } else if (obj instanceof Map) {
            trackMap(event, policyNode, (Map<?, ?>) obj, depth, isMicroservice);
        } else if (obj instanceof Map.Entry) {
            trackMapEntry(event, policyNode, (Map.Entry<?, ?>) obj, depth, isMicroservice);
        } else if (obj instanceof Collection && !(obj instanceof Enumeration)) {
            if (obj instanceof List) {
                trackList(event, policyNode, (List<?>) obj, depth, isMicroservice);
            } else {
                trackIterator(event, policyNode, ((Collection<?>) obj).iterator(), depth, isMicroservice);
            }
        } else if ("java.util.Optional".equals(obj.getClass().getName())) {
            trackOptional(event, policyNode, obj, depth, isMicroservice);
        } else {
            if (isSourceNode) {
                int len = TaintRangesBuilder.getLength(obj);
                if (identityHash == 0 || len == 0) {
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
                TaintPoolUtils.customModel(isMicroservice, obj, cls, event, policyNode, depth);
            } else {
                hash = getStringHash(obj);
                if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                    event.addSourceHash(hash);
                }
            }
        }
    }

    private static void customModel(Boolean isMicroservice, Object obj, Class<?> cls, MethodEvent event, PolicyNode policyNode, int depth) {
        if (isMicroservice && !(obj instanceof String) && !PropertyUtils.isDisabledCustomModel()) {
            try {
                Field[] declaredFields = ReflectUtils.getDeclaredFieldsSecurity(cls);
                for (Field field : declaredFields) {
                    if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic() && !field.isEnumConstant() && !(field.get(obj) instanceof Enumeration)) {
                        trackObject(event, policyNode, field.get(obj), depth + 1, isMicroservice);
                    }
                }
                long hash = System.identityHashCode(obj);
                if (EngineManager.TAINT_HASH_CODES.contains(hash)) {
                    event.addSourceHash(hash);
                }
            } catch (Throwable e) {
                DongTaiLog.debug("solve model failed: {}, {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            }
        }
    }

    private static void trackArray(MethodEvent event, PolicyNode policyNode, Object arr, int depth, Boolean isMicroservice) {
        int length = Array.getLength(arr);
        for (int i = 0; i < length; i++) {
            trackObject(event, policyNode, Array.get(arr, i), depth + 1, isMicroservice);
        }
    }

    private static void trackIterator(MethodEvent event, PolicyNode policyNode, Iterator<?> it, int depth, Boolean isMicroservice) {
        while (it.hasNext()) {
            trackObject(event, policyNode, it.next(), depth + 1, isMicroservice);
        }
    }

    private static void trackMap(MethodEvent event, PolicyNode policyNode, Map<?, ?> map, int depth, Boolean isMicroservice) {
        for (Object key : map.keySet()) {
            trackObject(event, policyNode, key, depth + 1, isMicroservice);
            trackObject(event, policyNode, map.get(key), depth + 1, isMicroservice);
        }
    }

    private static void trackMapEntry(MethodEvent event, PolicyNode policyNode, Map.Entry<?, ?> entry, int depth, Boolean isMicroservice) {
        trackObject(event, policyNode, entry.getKey(), depth + 1, isMicroservice);
        trackObject(event, policyNode, entry.getValue(), depth + 1, isMicroservice);
    }

    private static void trackList(MethodEvent event, PolicyNode policyNode, List<?> list, int depth, Boolean isMicroservice) {
        for (Object obj : list) {
            trackObject(event, policyNode, obj, depth + 1, isMicroservice);
        }
    }

    private static void trackOptional(MethodEvent event, PolicyNode policyNode, Object obj, int depth, Boolean isMicroservice) {
        try {
            Object v = ((Optional<?>) obj).orElse(null);
            trackObject(event, policyNode, v, depth + 1, isMicroservice);
        } catch (Throwable ignore) {
        }
    }

    public static Long toStringHash(long objectHashCode, long identityHashCode) {
        return (objectHashCode << 32) | (identityHashCode & 0xFFFFFFFFL);
    }

    public static Long getStringHash(Object obj) {
        long hash;
        if (obj instanceof String) {
            hash = TaintPoolUtils.toStringHash(obj.hashCode(), System.identityHashCode(obj));
        } else {
            hash = System.identityHashCode(obj);
        }
        return hash;
    }

}
