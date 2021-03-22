package com.secnium.iast.core.engines;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.engines.impl.SandboxEngine;
import org.junit.Test;

public class SandboxEngineTest {
    @Test
    public void init() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);

        SandboxEngine engine = new SandboxEngine();
        engine.init(propertiesUtils, null);
    }

    @Test
    public void start() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);

        SandboxEngine engine = new SandboxEngine();
        engine.init(propertiesUtils, null);
        engine.start();
    }

    @Test
    public void destory() {
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);

        SandboxEngine engine = new SandboxEngine();
        engine.init(propertiesUtils, null);
        engine.destroy();
    }
}
