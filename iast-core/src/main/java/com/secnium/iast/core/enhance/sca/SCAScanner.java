package com.secnium.iast.core.enhance.sca;

import com.secnium.iast.core.report.AssestReport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ScaScanner {
    private static final String ALGORITHM = "SHA-1";
    private static final String JAR = ".jar";

    public static void scan(File file) {
        String filePath = file.getPath();
        if (filePath.endsWith(JAR)) {
            if (file.exists()) {
                String packageName = file.getName();
                String signature = SignatureAlgorithm.getSignature(file, ScaScanner.ALGORITHM);
                if (null != packageName && null != signature) {
                    AssestReport.sendReport(filePath, packageName, signature, ScaScanner.ALGORITHM);
                }
            }
        }
    }

    public static InputStream getJarInputStream(String filePath, String name) throws Exception {
        URL url = new URL("jar:file:" + filePath + "!/" + name);
        JarURLConnection jarConnection = (JarURLConnection) url
                .openConnection();

        return jarConnection.getInputStream();
    }

    public static void scanWithJarPackage(String path) {
        try {
            JarFile file = new JarFile(path);
            Enumeration<JarEntry> entries = file.entries();
            String entryName;
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                entryName = entry.getName();
                if (entryName.endsWith(".jar")) {
                    InputStream is = getJarInputStream(path, entryName);
                    String signature = SignatureAlgorithm.getSignature(is, ScaScanner.ALGORITHM);
                    String packageName = entry.getName();
                    if (null != packageName && null != signature) {
                        AssestReport.sendReport("jar:file:" + path + "!/" + entryName, packageName, signature, ScaScanner.ALGORITHM);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
