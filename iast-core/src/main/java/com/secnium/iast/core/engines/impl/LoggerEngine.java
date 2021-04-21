package com.secnium.iast.core.engines.impl;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.IEngine;
import com.secnium.iast.core.util.NamespaceConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class LoggerEngine implements IEngine {
    private PropertyUtils cfg;
    private Instrumentation inst;

    @Override
    public void init(PropertyUtils cfg, Instrumentation inst) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        final Logger logger = LoggerFactory.getLogger(LoggerEngine.class);
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = LoggerEngine.class.getClassLoader().getResourceAsStream("dongtai-log.xml");
            configurator.doConfigure(configStream);
            logger.info("Log module initialized successfully");
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
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}
