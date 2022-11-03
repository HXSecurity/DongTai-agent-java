package io.dongtai.log;

import java.io.File;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {

    private static String dongtaiLog;
    private static String dongtaiLogDir;
    private static String dongtaiLogLevel;

    public static String enablePrintLog() {
        if (dongtaiLog == null) {
            dongtaiLog = System.getProperty("dongtai.log", "true");
        }
        return dongtaiLog;
    }

    public static String getLogDir() {
        if (dongtaiLogDir == null || dongtaiLogDir.isEmpty()) {
            String path = System.getProperty("dongtai.log.path");
            if (path != null && path.endsWith(File.separator)) {
                path = path.substring(0, path.length() - 1);
            }
            if (path != null && !path.isEmpty()) {
                dongtaiLogDir = path;
                return dongtaiLogDir;
            }

            String tmpDir = System.getProperty("java.io.tmpdir.dongtai");
            if (tmpDir != null && tmpDir.endsWith(File.separator)) {
                tmpDir = tmpDir.substring(0, tmpDir.length() - 1);
            }
            if (null == tmpDir || tmpDir.isEmpty()) {
                return "";
            }
            dongtaiLogDir = tmpDir + File.separator + "logs";
        }
        return dongtaiLogDir;
    }

    public static void setLogDir(String path) {
        dongtaiLogDir = path;
    }

    public static String getLogLevel() {
        if (dongtaiLogLevel == null) {
            dongtaiLogLevel = System.getProperty("dongtai.log.level", "info");
        }
        return dongtaiLogLevel;
    }
}
