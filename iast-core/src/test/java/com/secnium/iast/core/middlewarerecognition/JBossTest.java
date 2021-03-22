package com.secnium.iast.core.middlewarerecognition;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JBossTest {
    @Test
    public void getInstance() {
        final Pattern VER_PATTERN = Pattern.compile("<jar name=\"jboss-system.jar\" specVersion=\"(.*?)\"");
        String rootPath = "/Volumes/workspace/JobSpace/secnium/iast/IAST靶场基础环境/jboss-6.1.0.Final";
        String version = "*";
        File versionFile = new File(rootPath, "jar-versions.xml");
        if (versionFile.exists()) {
            File temp = new File(rootPath, "bin" + File.separatorChar + "run.jar");
            if (temp.exists()) {
                try {
                    byte[] arrayOfByte = FileUtils.readFileToByteArray(versionFile);
                    String str = new String(arrayOfByte);
                    Matcher matcher = VER_PATTERN.matcher(str);
                    if (matcher.find()) {
                        version = matcher.group(1);
                    }
                } catch (IOException iOException) {
                    ;
                }
            }
        }
        System.out.println("version = " + version);
    }
}
