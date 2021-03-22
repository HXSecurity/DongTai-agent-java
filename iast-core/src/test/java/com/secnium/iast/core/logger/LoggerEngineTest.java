package com.secnium.iast.core.logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.engines.impl.LoggerEngine;
import com.secnium.iast.core.util.NamespaceConvert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class LoggerEngineTest {
    @Test
    public void testSelfLogger() {
        final LoggerContext loggerContext = new LoggerContext();
//        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        final Logger logger = LoggerFactory.getLogger(LoggerEngine.class);

        InputStream configStream = null;
        try {
            NamespaceConvert.initNamespaceConvert("LingZhi");
            configStream = LoggerEngine.class.getClassLoader().getResourceAsStream("lingzhi-log.xml");
            configurator.doConfigure(configStream);
            logger.info("Log module initialized successfully");
            //logger.info(SandboxStringUtils.getLogo());
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

        logger.debug("hello");
    }
}
