package com.secnium.iast.core.enhance.sca;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Test;

public class ScaScanTest {

    @Test
    public void scan() throws MalformedURLException {
        String[] packagePaths = new String[]{
        };

        for (String packagePath : packagePaths) {
            ScaScanner.scanForSCA(new URL(packagePath), "");
        }
    }

    @Test
    public void scanWithJar() {
//        String path = "jar:file:～/workspace/secnium/BugPlatflam/dongtai/test-case/springsec/target/iast-vulns.jar!/BOOT-INF/lib/spring-core-5.2.8.RELEASE.jar!/";
//        ScaScanner.scanWithJarPackage(path);
    }
}