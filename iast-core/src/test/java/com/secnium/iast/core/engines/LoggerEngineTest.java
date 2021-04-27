package com.secnium.iast.core.engines;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.impl.LoggerEngine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerEngineTest {
    @Test
    public void start() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);
        LoggerEngine engine = new LoggerEngine();
        engine.init(propertiesUtils, null);

        engine.start();

        Logger logger = com.secnium.iast.core.AgentEngine.DEFAULT_LOGGERCONTEXT.getLogger(getClass());
        logger.info("LoggerEngineTest.start");
    }
}
