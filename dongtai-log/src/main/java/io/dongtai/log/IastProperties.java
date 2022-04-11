package io.dongtai.log;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {

    private static String dongtaiLog;
    private static String dongtaiLogPath;
    private static String dongtaiLogLevel;

    public static String enableLogFile() {
        if (dongtaiLog == null) {
            dongtaiLog = System.getProperty("dongtai.log", "true");
        }
        return dongtaiLog;
    }

    public static String getLogPath() {
        if (dongtaiLogPath == null) {
            dongtaiLogPath = System.getProperty("dongtai.log.path", System.getProperty("java.io.tmpdir.dongtai")+"/dongtaiJavaAgentLogs");
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
