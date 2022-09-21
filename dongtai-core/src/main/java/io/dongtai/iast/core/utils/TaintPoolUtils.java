package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

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
                isContains = contains(stringItem);
                if (isContains) {
                    event.addSourceHash(System.identityHashCode(obj));
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
        return isAllowTaintType(obj.getClass());
    }

    public static boolean isAllowTaintGetterMethod(Method method) {
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
            return false;
        }

        return isAllowTaintType(method.getReturnType());
    }
}
