package com.secnium.iast.core.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.engines.impl.LoggerEngine;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author WuHaoyuan
 * @since 2021-05-08 下午5:01
 */
public class LogUtils implements ILoggerFactory {

    private LoggerContext loggerContext;

    public LogUtils() {
        this.loggerContext = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = LoggerEngine.class.getClassLoader().getResourceAsStream("logback-dongtai.xml");
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
}
