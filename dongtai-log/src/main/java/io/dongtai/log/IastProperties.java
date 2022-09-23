package io.dongtai.log;

import java.io.File;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {

    private static String dongtaiLog;
    private static String dongtaiLogPath;
    private static String dongtaiLogLevel;

    public static String enablePrintLog() {
        if (dongtaiLog == null) {
            dongtaiLog = System.getProperty("dongtai.log", "true");
        }
        return dongtaiLog;
    }

    public static String getLogPath() {
        if (dongtaiLogPath == null) {
            String tmpDir = System.getProperty("java.io.tmpdir.dongtai");
            if (null == tmpDir || tmpDir.isEmpty()) {
                return "";
            }
            dongtaiLogPath = System.getProperty("dongtai.log.path",
                    tmpDir + File.separator + "logs");
        }
        return dongtaiLogPath;
    }

    public static String getLogLevel() {
        if (dongtaiLogLevel == null) {
            dongtaiLogLevel = System.getProperty("dongtai.log.level", "info");
        }
        return dongtaiLogLevel;
    }
}
