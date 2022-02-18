package io.dongtai.iast.core.bytecode.enhance.asm;

import io.dongtai.iast.core.utils.UnCaughtException;

import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author luanjia@taobao.com
 * @modify dongzhiyong@huoxian.cn
 */
public class SandboxReflectUtils {

    /**
     * 获取Java类的方法
     * 该方法不会抛出任何声明式异常
     *
     * @param clazz               类
     * @param name                方法名
     * @param parameterClassArray 参数类型数组
     * @return Java方法
     */
    public static Method unCaughtGetClassDeclaredJavaMethod(final Class<?> clazz,
                                                            final String name,
                                                            final Class<?>... parameterClassArray) {
        try {
            return clazz.getDeclaredMethod(name, parameterClassArray);
        } catch (NoSuchMethodException e) {
            throw new UnCaughtException(e);
        }
    }

}
