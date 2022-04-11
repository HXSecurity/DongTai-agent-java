package com.secnium.iast.core.middlewarerecognition;

import io.dongtai.log.DongTaiLog;
import org.junit.Test;

import java.io.*;

public class JettyTest {
    @Test
    public void getVersion() {
        File versionFile = null;
        FileReader fileReader = null;
        LineNumberReader reader = null;
        String version = "x";
        try {
            versionFile = new File("/Volumes/workspace/JobSpace/secnium/iast/IAST靶场基础环境/jetty-distribution-9.3.9.v20160517", "VERSION.txt");
            fileReader = new FileReader(versionFile);
            reader = new LineNumberReader(fileReader);
            String temp = reader.readLine();
            version = temp.split(" ")[0];
            reader.close();
            fileReader.close();
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
        System.out.println("version = " + version);
    }
}
