package com.secnium.iast.core.logger;

import com.secnium.iast.core.engines.impl.LoggerEngine;
import org.junit.Test;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

public class LoggerEngineTest {
    @Test
    public void testSelfLogger() {
        LoggerEngine loggerEngine = new LoggerEngine();
        loggerEngine.init(null, null);

        Logger logger = LogUtils.getLogger(LoggerEngineTest.class);
        logger.info("Log module initialized successfully");
        logger.debug("hello");
    }
}
