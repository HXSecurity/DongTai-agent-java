package com.secnium.iast.core.enhance.sca;

import org.junit.Test;

import java.io.File;

public class ScaScanTest {

    @Test
    public void scan() {
        String[] packagePaths = new String[]{};

        for (String packagePath : packagePaths) {
            ScaScanner.scan(new File(packagePath));
        }
    }

    @Test
    public void scanWithJar() {
//        String path = "jar:file:～/workspace/secnium/BugPlatflam/dongtai/test-case/springsec/target/iast-vulns.jar!/BOOT-INF/lib/spring-core-5.2.8.RELEASE.jar!/";
//        ScaScanner.scanWithJarPackage(path);
    }
}