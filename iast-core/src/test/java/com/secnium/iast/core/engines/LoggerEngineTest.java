package com.secnium.iast.core.engines;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.secnium.iast.core.engines.impl.LoggerEngine;
import com.secnium.iast.core.util.MyLoggerFactory;
import com.secnium.iast.core.util.NamespaceConvert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class LoggerEngineTest {
    @Test
    public void start() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        LoggerEngine engine = new LoggerEngine();
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("LoggerEngineTest.start");
    }

    @Test
    public void testCustomLogger() {
//        final LoggerContext loggerContext = new LoggerContext();
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        InputStream configStream = null;
        final Logger logger = LoggerFactory.getLogger(LoggerEngineTest.class);
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
        logger.debug("hello");
        logger.info("Log module initialized successfully");
        logger.warn("warnnnnn");
    }

    @Test
    public void testCustomLogger2() {
        LoggerContext loggerContext = new LoggerContext();
        //final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        //loggerContext.reset();
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
        //LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger logger = LoggerFactory.getLogger(LoggerEngine.class);
        //Logger logger = loggerContext.getLogger(LoggerEngine.class);
        logger.debug("hello");
        logger.info("Log module initialized successfully");
        logger.warn("warnnnnn");
    }

    @Test
    public void testCustomLogger3(){
        Logger logger = MyLoggerFactory.getLogger(LoggerEngine.class);
        logger.debug("hello");
        logger.info("Log module initialized successfully");
        logger.warn("warnnnnn");
    }
}
