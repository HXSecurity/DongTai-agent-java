package io.dongtai.iast.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ReflectUtils {

    public static Field getFieldFromClass(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    public static Field getDeclaredFieldFromClassByName(Class<?> cls, String fieldName) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (fieldName.equals(field.getName())) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static Field getDeclaredFieldFromSuperClassByName(Class<?> cls, String fieldName) {
        for (Class<?> spc = cls; Object.class != spc && spc != null; spc = spc.getSuperclass()) {
            Field field = getDeclaredFieldFromClassByName(spc, fieldName);
            if (field != null) {
                return field;
            }
        }
        return null;
    }

    public static Field getRecursiveField(Class<?> cls, String fieldName) {
        if (cls == null || cls == Object.class) {
            return null;
        }

        try {
            return ReflectUtils.getFieldFromClass(cls, fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = cls.getSuperclass();
            return getRecursiveField(superclass, fieldName);
        }
    }

    public static Method getPublicMethodFromClass(Class<?> cls, String method) throws NoSuchMethodException {
        return getPublicMethodFromClass(cls, method, ObjectShare.EMPTY_CLASS_ARRAY);
    }

    public static Method getPublicMethodFromClass(Class<?> cls, String methodName, Class<?>[] parameterTypes) throws NoSuchMethodException {
        Method method = cls.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    public static Method getDeclaredMethodFromClass(Class<?> cls, String methodName, Class<?>[] parameterTypes) {
        Method[] methods = cls.getDeclaredMethods();
        if (parameterTypes == null) {
            parameterTypes = ObjectShare.EMPTY_CLASS_ARRAY;
        }
        for (Method method : methods) {
            if (methodName.equals(method.getName()) && Arrays.equals(parameterTypes, method.getParameterTypes())) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    public static Method getDeclaredMethodFromSuperClass(Class<?> cls, String methodName, Class<?>[] parameterTypes) {
        if (cls == Object.class) {
            return null;
        }
        Method method = getDeclaredMethodFromClass(cls, methodName, parameterTypes);
        if (method == null) {
            return getDeclaredMethodFromSuperClass(cls.getSuperclass(), methodName, parameterTypes);
        }
        return method;
    }

    public static boolean isDescendantOf(Class<?> cls, String className) {
        if (cls == null) {
            return false;
        }

        while (!Object.class.equals(cls)) {
            if (className.equals(cls.getName())) {
                return true;
            } else {
                cls = cls.getSuperclass();
            }
        }
        return "java.lang.Object".equals(className);
    }

    public static boolean isImplementsInterface(Class<?> cls, String interfaceName) {
        if (cls == null) {
            return false;
        }
        if (cls.isInterface()) {
            if (interfaceName.equals(cls.getName())) {
                return true;
            }
            for (Class<?> itf : cls.getInterfaces()) {
                if (isImplementsInterface(itf, interfaceName)) {
                    return true;
                }
            }
            return false;
        }
        while (cls != null) {
            for (Class<?> itf : cls.getInterfaces()) {
                if (isImplementsInterface(itf, interfaceName)) {
                    return true;
                }
            }
            cls = cls.getSuperclass();
        }
        return false;
    }

    public static List<Class<?>> getAllInterfaces(Class<?> cls) {
        ArrayList<Class<?>> interfaceList = new ArrayList<Class<?>>();
        if (cls == null) {
            return interfaceList;
        }
        getAllInterfaces(cls, interfaceList);
        return interfaceList;
    }

    private static void getAllInterfaces(Class<?> cls, List<Class<?>> interfaceList) {
        while (cls != null) {
            Class<?>[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (!interfaceList.contains(interfaces[i])) {
                    interfaceList.add(interfaces[i]);
                    getAllInterfaces(interfaces[i], interfaceList);
                }
            }
            cls = cls.getSuperclass();
        }
    }
}
