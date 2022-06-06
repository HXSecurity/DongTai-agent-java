package io.dongtai.iast.core;

import io.dongtai.iast.core.bytecode.sca.ScaScanner;
import io.dongtai.iast.core.bytecode.sca.SignatureAlgorithm;
import io.dongtai.iast.core.handler.hookpoint.vulscan.ReportConstant;
import io.dongtai.log.DongTaiLog;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Scatest {

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(15);
        scanJarLib("/Users/erzhuangniu/Desktop/Desktop0301/webgoat-server-8.2.2.jar");
        TimeUnit.SECONDS.sleep(5);
        TimeUnit.MILLISECONDS.sleep(500);
    }


    private static void scanJarLib(String packagePath) {
        try {
            JarFile file = new JarFile(packagePath);
            Enumeration<JarEntry> entries = file.entries();
            String entryName;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                entryName = entry.getName();
                if (entryName.endsWith(".jar")) {
//                    DongTaiLog.info(entryName);
//                    TimeUnit.SECONDS.sleep(1);
                    InputStream is = getJarInputStream(packagePath, entryName);
                    String signature = SignatureAlgorithm.getSignature(is, "SHA-1");
                    String packageName = entry.getName();
                    if (signature == null) {
                        continue;
                    }
                    JSONObject packageObj = new JSONObject();
                    packageObj.put(ReportConstant.SCA_PACKAGE_PATH, "jar:file:" + packagePath + "!/" + entryName);
                    packageObj.put(ReportConstant.SCA_PACKAGE_NAME, packageName);
                    packageObj.put(ReportConstant.SCA_PACKAGE_SIGNATURE, signature);
                    packageObj.put(ReportConstant.SCA_PACKAGE_ALGORITHM, "SHA-1");
                }
            }
        } catch (Exception e) {
            DongTaiLog.error(e.getMessage());
        }
    }

    public static InputStream getJarInputStream(String filePath, String name) throws Exception {
        URL url = new URL("jar:file:" + filePath + "!/" + name);
        JarURLConnection jarConnection = (JarURLConnection) url
                .openConnection();

        return jarConnection.getInputStream();
    }

}
