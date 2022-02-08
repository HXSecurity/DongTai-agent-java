package com.secnium.iast.core.engines;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.impl.ConfigEngine;
import org.junit.Test;

public class ConfigEngineTest {

    @Test
    public void start() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);

        ConfigEngine engine = new ConfigEngine();
        engine.init(propertiesUtils, null);

        engine.start();
    }
}
