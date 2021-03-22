package com.secnium.iast.core.enhance.sca;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class ManifestScanTest {

    @Test
    public void parseJarManiifest() {
        String filename = "/Volumes/workspace/JobSpace/secnium/iast/agent_example/iast_test/apache-tomcat-8.5.40/lib/catalina.jar";
        filename = "~/.m2/repository/org/apache/shiro/shiro-web/1.5.2/shiro-web-1.5.2.jar";
        try {
            File file = new File(filename);
            if (file.exists()) {
                String manifestes = ManifestScaner.parseJarManifest(new JarFile(file));
                System.out.println("manifestes = " + manifestes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}