package io.dongtai.iast.core.bytecode.sca;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.ReportKey;
import io.dongtai.iast.common.constants.ReportType;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ScaScanner {

    private static final String ALGORITHM = "SHA-1";
    private static final String JAR = ".jar";
    private static volatile HashSet<String> scannedClassSet = new HashSet<String>();
    private static volatile HashSet<String> scaSet = new HashSet<String>();
    private static volatile Boolean isClassPath = false;

    private static boolean isJarLibs(String packageFile) {
        return packageFile.startsWith("file:") && packageFile.endsWith(".jar!/") && packageFile.contains("BOOT-INF");
    }

    private static boolean isWarLibs(String packageFile) {
        return packageFile.endsWith(".jar") && packageFile.contains("WEB-INF");
    }

    private static boolean isLocalMavenRepo(String packageFile) {
        return packageFile.endsWith(".jar") && packageFile.contains("repository");
    }

    /**
     * @param packageFile
     * @param internalClassName
     */
    public static void scanForSCA(String packageFile, String internalClassName) {
        File jarPackageFile = new File(packageFile);
        String packagePath = jarPackageFile.getParent();
        if (isJarLibs(packageFile)) {
            packageFile = packageFile.replace("file:", "");
            packageFile = packageFile.substring(0, packageFile.indexOf("!/"));
            if (!scannedClassSet.contains(packageFile)) {
                scannedClassSet.add(packageFile);
                ThreadPools.execute(new ScaScanThread(packageFile, 2));
            }
        } else if (isWarLibs(packageFile) && !scannedClassSet.contains(packagePath)) {
            scannedClassSet.add(packagePath);
            ThreadPools.execute(new ScaScanThread(packagePath, 1));
        } else if (!scannedClassSet.contains(packageFile) && isLocalMavenRepo(packageFile)) {
            scannedClassSet.add(packageFile);
            ThreadPools.execute(new ScaScanThread(packageFile, 3));
        } else if (packageFile.endsWith(".jar") && !scaSet.contains(packageFile)) {
            scaSet.add(packageFile);
            ThreadPools.execute(new ScaScanThread(packageFile, 3));
        } else if (!scaSet.contains(packageFile) && isLocalMavenRepo(packageFile)) {
            scaSet.add(packageFile);
        }
        if (!isClassPath) {
            isClassPath = true;
            ThreadPools.execute(new ScaScanThread(System.getProperty("java.class.path"), 4));
        }
    }

    /**
     * Asynchronous analysis of third-party dependent components
     */
    private static class ScaScanThread extends Thread {

        private final String packagePath;
        private final int scaType;
        private final JSONObject scaReport;
        private final JSONArray packages;

        /**
         * Allocates a new {@code Thread} object. This constructor has the same effect as  {@code (null, null, gname)},
         * where {@code gname} is a newly generated name. Automatically generated names are of the form {@code
         * "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
         */
        public ScaScanThread(String packagePath, int scaType) {
            this.packagePath = packagePath;
            this.scaType = scaType;
            this.scaReport = new JSONObject();
            this.packages = new JSONArray();
            scaReport.put(ReportKey.TYPE, ReportType.SCA_BATCH);
            JSONObject detail = new JSONObject();
            detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
            detail.put(ScaReport.KEY_SCA_PACKAGES, packages);
            scaReport.put(ReportKey.DETAIL, detail);
        }

        public void scan(File file) {
            String filePath = file.getPath();
            if (filePath.endsWith(JAR)) {
                if (file.exists()) {
                    String packageName = file.getName();
                    String signature = SignatureAlgorithm.getSignature(file, ScaScanner.ALGORITHM);
                    if (null != signature) {
                        JSONObject packageObj = new JSONObject();
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_PATH, packagePath);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_NAME, packageName);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_SIGNATURE, signature);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_ALGORITHM, ScaScanner.ALGORITHM);
                        packages.add(packageObj);
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

        public void scanClassPath(String packagesPath) {
            String osName = System.getProperty("os.name").toLowerCase();
            String[] packages;
            if (osName.contains("windows")) {
                packages = packagesPath.split(";");
            } else {
                packages = packagesPath.split(":");
            }
            for (String packagePath : packages) {
                if (packagePath.endsWith(JAR)) {
                    File file = new File(packagePath);
                    JSONObject packageObj = new JSONObject();
                    packageObj.put(ScaReport.KEY_SCA_PACKAGE_PATH, packagePath);
                    packageObj.put(ScaReport.KEY_SCA_PACKAGE_NAME, file.getName());
                    packageObj.put(ScaReport.KEY_SCA_PACKAGE_SIGNATURE,
                            SignatureAlgorithm.getSignature(file, ScaScanner.ALGORITHM));
                    packageObj.put(ScaReport.KEY_SCA_PACKAGE_ALGORITHM, ScaScanner.ALGORITHM);
                    this.packages.add(packageObj);
                }
            }
        }

        private void scanWarLib(String packagePath) {
            File packagePathFile = new File(packagePath);
            File[] packagePathFiles = packagePathFile.listFiles();
            for (File tempPackagePathFile : packagePathFiles != null ? packagePathFiles : new File[0]) {
                scan(tempPackagePathFile);
            }
        }


        private void scanJarLib(String packagePath) {
            try {
                JarFile file = new JarFile(packagePath);
                Enumeration<JarEntry> entries = file.entries();
                String entryName;
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    entryName = entry.getName();
                    if (entryName.endsWith(".jar")) {
                        InputStream is = getJarInputStream(packagePath, entryName);
                        String signature = SignatureAlgorithm.getSignature(is, ScaScanner.ALGORITHM);
                        String packageName = entry.getName();
                        if (signature == null) {
                            continue;
                        }
                        JSONObject packageObj = new JSONObject();
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_PATH, "jar:file:" + packagePath + "!/" + entryName);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_NAME, packageName);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_SIGNATURE, signature);
                        packageObj.put(ScaReport.KEY_SCA_PACKAGE_ALGORITHM, ScaScanner.ALGORITHM);
                        packages.add(packageObj);
                    }
                }
            } catch (Throwable e) {
                DongTaiLog.warn(ErrorCode.get("SCA_SCAN_JAR_LIB_FAILED"),
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
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
         */
        @Override
        public void run() {
            try {
                switch (scaType) {
                    case 1:
                        scanWarLib(packagePath);
                        break;
                    case 2:
                        scanJarLib(packagePath);
                        break;
                    case 3:
                        scan(new File(packagePath));
                        break;
                    case 4:
                        scanClassPath(packagePath);
                        break;
                    default:
                        break;
                }
                ScaReport.sendReport(this.scaReport.toString());
            } catch (Throwable ignore) {
            }
        }
    }
}
