package com.secnium.iast.core.engines;

import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.init.impl.ConfigEngine;
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
