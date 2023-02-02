package io.dongtai.log;

import java.io.File;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {
    public static boolean isEnabled() {
        return !"false".equalsIgnoreCase(System.getProperty("dongtai.log", "true"));
    }

    public static String getLogDir() {
        String path = System.getProperty("dongtai.log.path");
        if (path != null && path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        if (path != null && !path.isEmpty()) {
            return path;
        }

        String tmpDir = System.getProperty("java.io.tmpdir.dongtai");
        if (tmpDir != null && tmpDir.endsWith(File.separator)) {
            tmpDir = tmpDir.substring(0, tmpDir.length() - 1);
        }
        if (null == tmpDir || tmpDir.isEmpty()) {
            return "";
        }
        return tmpDir + File.separator + "logs";
    }

    public static String getLogLevel() {
        return System.getProperty("dongtai.log.level", "info");
    }
}
