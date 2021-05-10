package com.secnium.iast.core.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author WuHaoyuan
 */
public class LogUtils implements ILoggerFactory {

    private final LoggerContext loggerContext;

    private static LogUtils logUtils;

    /**
     * 读取默认的logback配置文件
     */
    private LogUtils() {
        this.loggerContext = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = LogUtils.class.getClassLoader().getResourceAsStream("logback-dongtai.xml");
            configurator.doConfigure(configStream);
        } catch (JoranException e) {
            e.printStackTrace();
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Logger getLogger(String s) {
        return this.loggerContext.getLogger(s);
    }

    public static Logger getLogger(Class<?> clazz) {
        if (logUtils == null) {
            logUtils = new LogUtils();
        }
        return logUtils.getLogger(clazz.getName());
    }
}
