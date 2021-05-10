package com.secnium.iast.core.util;

import com.secnium.iast.core.report.ErrorLogReport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ConfigUtils {
    private final static Logger logger = LogUtils.getLogger(ConfigUtils.class);

    /**
     * 通过文件名从资源加载器中加载资源
     *
     * @param filename
     * @return
     */
    public static InputStream getResourceAsStreamFromFilename(String filename) {
        return ConfigUtils.class.getClassLoader().getResourceAsStream(filename);
    }

    public static HashSet[] loadConfigFromFile(String filename) {
        HashSet<String> container = new HashSet<String>();
        HashSet<String> startWith = new HashSet<String>();
        HashSet<String> endWith = new HashSet<String>();
        InputStream fis = null;
        try {
            fis = getResourceAsStreamFromFilename(filename);
            LineIterator lineIterator = IOUtils.lineIterator(fis, (String) null);
            while (lineIterator.hasNext()) {
                String className = lineIterator.nextLine().trim();
                if (!className.startsWith("#")) {
                    if (className.startsWith("*")) {
                        endWith.add(className.substring(1));
                    } else if (className.endsWith("*")) {
                        startWith.add(className.substring(0, className.length() - 1));
                    } else {
                        container.add(className);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("读取配置文件：{} 失败，错误信息：{}", filename, e);
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return new HashSet[]{container, startWith, endWith};
    }

    public static String[] loadExtConfigFromFile(String filename) {
        InputStream fis = null;
        String[] extStringArray = null;
        try {
            fis = getResourceAsStreamFromFilename(filename);
            LineIterator lineIterator = IOUtils.lineIterator(fis, (String) null);
            while (lineIterator.hasNext()) {
                String exts = lineIterator.nextLine().trim();
                extStringArray = exts.split(",");
            }
        } catch (IOException e) {
            logger.error("读取后缀配置文件：{} 失败，错误信息：{}", filename, e);
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return extStringArray;
    }
}
