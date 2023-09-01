package io.dongtai.iast.core.utils;

import io.dongtai.log.DongTaiLog;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ReflectUtils {

    public static Field getFieldFromClass(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field field = cls.getDeclaredField(fieldName);
        setAccessible(field);
        return field;
    }

    public static Field getDeclaredFieldFromClassByName(Class<?> cls, String fieldName) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (fieldName.equals(field.getName())) {
                setAccessible(field);
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
        return getSecurityPublicMethod(method);
    }

    public static Method getSecurityPublicMethod(Method method) throws NoSuchMethodException {
        if (hasNotSecurityManager()) {
            setAccessible(method);
            return method;
        }
        return AccessController.doPrivileged((PrivilegedAction<Method>) () -> {
            setAccessible(method);
            return method;
        });
    }

    public static Method getDeclaredMethodFromClass(Class<?> cls, String methodName, Class<?>[] parameterTypes) {
        Method[] methods = cls.getDeclaredMethods();
        if (parameterTypes == null) {
            parameterTypes = ObjectShare.EMPTY_CLASS_ARRAY;
        }
        for (Method method : methods) {
            if (methodName.equals(method.getName()) && Arrays.equals(parameterTypes, method.getParameterTypes())) {
                try {
                    return getSecurityPublicMethod(method);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
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
            for (Class<?> anInterface : interfaces) {
                if (!interfaceList.contains(anInterface)) {
                    interfaceList.add(anInterface);
                    getAllInterfaces(anInterface, interfaceList);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    public static Field[] getDeclaredFieldsSecurity(Class<?> cls) {
        Objects.requireNonNull(cls);
        if (hasNotSecurityManager()) {
            return getDeclaredFields(cls);
        }
        return AccessController.doPrivileged((PrivilegedAction<Field[]>) () -> getDeclaredFields(cls));
    }

    private static Field[] getDeclaredFields(Class<?> cls) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            setAccessible(field);
        }
        return declaredFields;
    }

    private static boolean hasNotSecurityManager() {
        return System.getSecurityManager() == null;
    }

    private static void setAccessible(AccessibleObject accessibleObject) {
        try{
            if (!accessibleObject.isAccessible()) {
                accessibleObject.setAccessible(true);
            }
        } catch (Throwable e){
            DongTaiLog.debug("setAccessible failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }

    }
}
