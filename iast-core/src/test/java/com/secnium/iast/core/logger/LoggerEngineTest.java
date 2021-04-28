package com.secnium.iast.core.logger;

import com.secnium.iast.core.engines.impl.LoggerEngine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerEngineTest {
    @Test
    public void testSelfLogger() {
        LoggerEngine loggerEngine = new LoggerEngine();
        loggerEngine.init(null, null);

        Logger logger = LoggerFactory.getLogger(LoggerEngineTest.class);
        logger.info("Log module initialized successfully");
        logger.debug("hello");
    }
}
