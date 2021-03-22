package com.secnium.iast.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtils {
    private static final Class<?>[] basetype = {
            Boolean.class,
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class
    };

    private static Map<String, String> basetypeMap = new HashMap();

    static {
        basetypeMap.put("[Z", "boolean[]");
        basetypeMap.put("[B", "byte[]");
        basetypeMap.put("[C", "char[]");
        basetypeMap.put("[S", "short[]");
        basetypeMap.put("[I", "int[]");
        basetypeMap.put("[J", "long[]");
        basetypeMap.put("[D", "double[]");
        basetypeMap.put("[F", "float[]");
    }

    public static Field getFieldFromObject(Object paramObject, String paramString) throws NoSuchFieldException {
        Field field = paramObject.getClass().getDeclaredField(paramString);
        field.setAccessible(true);
        return field;
    }

    public static Field getFieldFromClass(Class<?> paramClass, String paramString) throws NoSuchFieldException {
        Field field = paramClass.getDeclaredField(paramString);
        field.setAccessible(true);
        return field;
    }

    public static Field[] getFieldsFromClass(Class<?> paramClass) {
        Field[] fields = paramClass.getDeclaredFields();
        Field[] fieldsCopy = fields;
        int i;
        byte b1;
        for (i = fieldsCopy.length, b1 = 0; b1 < i; ) {
            Field field = fieldsCopy[b1];
            field.setAccessible(true);
            b1++;
        }

        return fields;
    }

    public static Field getPublicFieldFromObject(Object paramObject, String paramString) throws NoSuchFieldException {
        Field field = paramObject.getClass().getField(paramString);
        field.setAccessible(true);
        return field;
    }

    public static Field getPublicFieldFromClass(Class<?> paramClass, String paramString) throws NoSuchFieldException {
        Field field = paramClass.getField(paramString);
        field.setAccessible(true);
        return field;
    }

    public static Field[] getPublicFields(Class<?> paramClass) {
        Field[] fields = paramClass.getFields();
        Field[] fieldsCopy = fields;
        int i;
        byte b1;
        for (i = fieldsCopy.length, b1 = 0; b1 < i; ) {
            Field field = fieldsCopy[b1];
            field.setAccessible(true);
            b1++;
        }

        return fields;
    }

    public static Method getPublicMethodFromClassWithArg(Class<?> paramClass, String paramString, Class<?>... paramVarArgs) throws NoSuchMethodException {
        Method method = paramClass.getMethod(paramString, paramVarArgs);
        method.setAccessible(true);
        return method;
    }


    public static Method getPublicMethodFromClass(Class<?> paramClass, String paramString) throws NoSuchMethodException {
        Method method = paramClass.getMethod(paramString, ObjectShare.EMPTY_CLASS_ARRAY);
        method.setAccessible(true);
        return method;
    }


    public static Method getMethodFromClassWithArgs(Class<?> paramClass, String paramString, Class<?>... paramVarArgs) throws NoSuchMethodException {
        Method method = paramClass.getDeclaredMethod(paramString, paramVarArgs);
        method.setAccessible(true);
        return method;
    }


    public static Method getMethodFromClass(Class<?> paramClass, String paramString) throws NoSuchMethodException {
        Method method = paramClass.getDeclaredMethod(paramString, ObjectShare.EMPTY_CLASS_ARRAY);
        method.setAccessible(true);
        return method;
    }


    public static Constructor getPublicConstructorFromClassWithArgs(Class<?> paramClass, Class<?>... paramVarArgs) throws NoSuchMethodException {
        Constructor constructor = paramClass.getConstructor(paramVarArgs);
        constructor.setAccessible(true);
        return constructor;
    }


    public static Constructor getPublicConstructorFromClass(Class<?> paramClass) throws NoSuchMethodException {
        Constructor constructor = paramClass.getConstructor(ObjectShare.EMPTY_CLASS_ARRAY);
        constructor.setAccessible(true);
        return constructor;
    }


    public static Constructor getConstructorFromClassWithArgs(Class<?> paramClass, Class<?>... paramVarArgs) throws NoSuchMethodException {
        Constructor constructor = paramClass.getDeclaredConstructor(paramVarArgs);
        constructor.setAccessible(true);
        return constructor;
    }


    public static Constructor getConstructorFromClass(Class<?> paramClass) throws NoSuchMethodException {
        Constructor constructor = paramClass.getDeclaredConstructor(ObjectShare.EMPTY_CLASS_ARRAY);
        constructor.setAccessible(true);
        return constructor;
    }
}
