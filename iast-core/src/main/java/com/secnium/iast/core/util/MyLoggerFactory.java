package com.secnium.iast.core.util;

import org.slf4j.Logger;

/**
 * @author WuHaoyuan
 * @since 2021-05-08 下午5:35
 */
public class MyLoggerFactory {

    private static LogUtils logUtils;

    public static Logger getLogger(Class<?> clazz) {
        if(logUtils==null){
            logUtils=new LogUtils();
        }
        return logUtils.getLogger(clazz.getName());
    }
}
