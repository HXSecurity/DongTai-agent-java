package com.secnium.iast.core.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.engines.impl.LoggerEngine;
import com.secnium.iast.core.util.NamespaceConvert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class LoggerEngineTest {
    @Test
    public void testSelfLogger() {
        LoggerContext loggerContext = new LoggerContext();
        //final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        //loggerContext.reset();
        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("DongTai");
            configStream = LoggerEngine.class.getClassLoader().getResourceAsStream("dongtai-log.xml");
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
        Logger logger = loggerContext.getLogger(LoggerEngine.class);
        //Logger logger = LoggerFactory.getLogger(LoggerEngine.class);
        logger.info("Log module initialized successfully");
        logger.debug("hello");
    }
}
