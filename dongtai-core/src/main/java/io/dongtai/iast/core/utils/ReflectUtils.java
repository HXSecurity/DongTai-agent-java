package io.dongtai.iast.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ReflectUtils {

    public static Field getFieldFromClass(Class<?> paramClass, String paramString) throws NoSuchFieldException {
        Field field = paramClass.getDeclaredField(paramString);
        field.setAccessible(true);
        return field;
    }


    public static Method getPublicMethodFromClass(Class<?> paramClass, String paramString)
            throws NoSuchMethodException {
        Method method = paramClass.getMethod(paramString, ObjectShare.EMPTY_CLASS_ARRAY);
        method.setAccessible(true);
        return method;
    }


}
