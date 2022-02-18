package io.dongtai.iast.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类
 *
 * @author luanjia@taobao.com
 * @date 15/5/18
 * @modify dongzhiyong@huoxian.cn
 */
public class SandboxStringUtils {

    /**
     * java'TomcatV7 classname to internal'TomcatV7 classname
     *
     * @param javaClassName java'TomcatV7 classname
     * @return internal'TomcatV7 classname
     */
    public static String toInternalClassName(String javaClassName) {
        if (StringUtils.isEmpty(javaClassName)) {
            return javaClassName;
        }
        return javaClassName.replace('.', '/');
    }

    /**
     * internal'TomcatV7 classname to java'TomcatV7 classname
     * java/lang/String to java.lang.String
     *
     * @param internalClassName internal'TomcatV7 classname
     * @return java'TomcatV7 classname
     */
    public static String toJavaClassName(String internalClassName) {
        if (StringUtils.isEmpty(internalClassName)) {
            return internalClassName;
        }
        return internalClassName.replace('/', '.');
    }

}

