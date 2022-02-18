package com.secnium.iast.core;

import io.dongtai.iast.core.utils.PropertyUtils;
import org.junit.Test;

public class PropertiesUtilsTest {
    @Test
    public void getInstance() {
        System.out.println("[+] test init properties class");
        String propertiesFilePath = "～/Documents/workspace/BugPlatflam/IAST/IastDocker/SecniumIAST/release/config/iast.properties";
        PropertyUtils propertiesUtils = PropertyUtils.getInstance(propertiesFilePath);
        assert null != propertiesUtils;
    }
}
