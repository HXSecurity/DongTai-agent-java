package com.secnium.iast.core.enhance.sca;

import com.secnium.iast.core.report.AssestReport;
import com.secnium.iast.core.util.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ScaScanner {

    private static final String ALGORITHM = "SHA-1";
    private static final String JAR = ".jar";
    private static volatile HashSet<String> scannedClassSet = new HashSet<String>();
    private static Boolean isClassPath = false;

    /**
     * @param url
     * @param internalClassName
     */
    public static void scanForSCA(URL url, String internalClassName) {
        if (url == null
                || internalClassName == null
                || internalClassName.startsWith("com/secnium/iast/")
                || internalClassName.startsWith("java/lang/iast/")
                || internalClassName.startsWith("cn/huoxian/iast/")
                || internalClassName.startsWith("com/sun/")
                || internalClassName.startsWith("sun/")
        ) {
            return;
        }
        isClassPathStart();
        ScaScanThread scanThread = new ScaScanThread(url);
        scanThread.start();
    }

    private static void isClassPathStart() {
        if (!isClassPath) {
            String property = System.getProperty("java.class.path");
            String[] split = property.split(":");
            for (String string : split) {
                try {
                    File file = new File(string);
                    URL url = file.toURI().toURL();
                    ScaScanThread scanThread = new ScaScanThread(url);
                    scanThread.start();
                } catch (MalformedURLException e) {
                    Logger logger = LogUtils.getLogger(ScaScanner.class);
                    logger.error(e.getMessage());
                }
            }
            isClassPath = true;
        }
    }

    /**
     * Asynchronous analysis of third-party dependent components
     */
    private static class ScaScanThread extends Thread {

        private final URL packageUrl;

        /**
         * Allocates a new {@code Thread} object. This constructor has the same effect as {@linkplain
         * #Thread(ThreadGroup, Runnable, String) Thread} {@code (null, null, gname)}, where {@code gname} is a newly
         * generated name. Automatically generated names are of the form {@code "Thread-"+}<i>n</i>, where <i>n</i> is
         * an integer.
         */
        public ScaScanThread(URL packageUrl) {
            this.packageUrl = packageUrl;
        }

        public void scan(File file) {
            String filePath = file.getPath();
            if (filePath.endsWith(JAR)) {
                if (file.exists()) {
                    String packageName = file.getName();
                    String signature = SignatureAlgorithm.getSignature(file, ScaScanner.ALGORITHM);
                    if (null != signature) {
                        AssestReport.sendReport(filePath, packageName, signature, ScaScanner.ALGORITHM);
                    }
                }
            }
        }

        public InputStream getJarInputStream(String filePath, String name) throws Exception {
            URL url = new URL("jar:file:" + filePath + "!/" + name);
            JarURLConnection jarConnection = (JarURLConnection) url
                    .openConnection();

            return jarConnection.getInputStream();
        }

        public void scanWithJarPackage(String path) {
            try {
                JarFile file = new JarFile(path);
                Enumeration<JarEntry> entries = file.entries();
                String entryName;
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    entryName = entry.getName();
                    if (!entryName.endsWith(".jar")) {
                        continue;
                    }
                    InputStream is = getJarInputStream(path, entryName);
                    String signature = SignatureAlgorithm.getSignature(is, ScaScanner.ALGORITHM);
                    String packageName = entry.getName();
                    if (signature == null) {
                        continue;
                    }
                    AssestReport.sendReport("jar:file:" + path + "!/" + entryName, packageName, signature,
                            ScaScanner.ALGORITHM);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         * @see #Thread(ThreadGroup, Runnable, String)
         */
        @Override
        public void run() {
            String jarPackageFilePath = packageUrl.getFile();
            File jarPackageFile = new File(jarPackageFilePath);
            String packagePath = jarPackageFile.getParent();
            if (!scannedClassSet.contains(packagePath) && jarPackageFilePath.startsWith("file:") && jarPackageFilePath
                    .endsWith(".jar!/") && jarPackageFilePath.contains("BOOT-INF")) {
                scannedClassSet.add(packagePath);
                jarPackageFilePath = jarPackageFilePath.replace("file:", "");
                jarPackageFilePath = jarPackageFilePath.substring(0, jarPackageFilePath.indexOf("!/"));
                this.scanWithJarPackage(jarPackageFilePath);
            } else if (!scannedClassSet.contains(packagePath) && jarPackageFilePath.endsWith(".jar")
                    && jarPackageFilePath.contains("WEB-INF")) {
                scannedClassSet.add(packagePath);
                File packagePathFile = new File(packagePath);
                File[] packagePathFiles = packagePathFile.listFiles();
                for (File tempPackagePathFile : packagePathFiles != null ? packagePathFiles : new File[0]) {
                    scan(tempPackagePathFile);
                }
            } else if (!scannedClassSet.contains(jarPackageFilePath) && jarPackageFilePath.endsWith(".jar")
                    && jarPackageFilePath.contains("repository")) {
                scannedClassSet.add(jarPackageFilePath);
                scan(jarPackageFile);
            }
        }
    }
}
