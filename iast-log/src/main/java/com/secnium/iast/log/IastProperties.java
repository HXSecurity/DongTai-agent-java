package com.secnium.iast.log;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {

    private static IastProperties instance;
    public Properties cfg = new Properties();

    private String propertiesFilePath;

    public static IastProperties getInstance() {
        if (null == instance) {
            instance = new IastProperties(null);
        }
        return instance;
    }

    private IastProperties(String path) {
        try {
            init(path);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public void init(String path) throws ClassNotFoundException {
        String basePath = null;
        File agentFile;
        File propertiesFile;
        try {
            if (path != null) {
                propertiesFilePath = path;
            } else {
                agentFile = new File(
                        IastProperties.class.getProtectionDomain().getCodeSource().getLocation().getFile());
                basePath = agentFile.getParentFile().getPath();
                propertiesFilePath = basePath + File.separator + "config" + File.separator + "iast.properties";
                propertiesFilePath = URLDecoder.decode(propertiesFilePath, "utf-8");
            }

            propertiesFile = new File(propertiesFilePath);

            if (!propertiesFile.exists()) {
                if (!propertiesFile.getParentFile().exists()) {
                    if (!propertiesFile.getParentFile().mkdirs()) {
                        throw new NullPointerException("配置文件创建失败");
                    }
                }
                propertiesFile.createNewFile();
            }

            InputStream is = IastProperties.class.getClassLoader().getResourceAsStream("iast.properties");
            FileOutputStream fos = new FileOutputStream(propertiesFile);
            byte[] data = new byte[1024];
            while (true) {
                assert is != null;
                int length = is.read(data);
                if (length < 1024) {
                    fos.write(data, 0, length);
                    break;
                }
                fos.write(data);
            }

            is.close();
            fos.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            cfg.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String enableLogFile() {
        return System.getProperty("dongtai.log", cfg.getProperty("dongtai.log", "true"));
    }

    public String getLogPath() {
        return System.getProperty("dongtai.log.path", cfg.getProperty("dongtai.log.path", "logs"));
    }

    public String getLogLevel() {
        return System.getProperty("dongtai.log.level", cfg.getProperty("dongtai.log.level", "info"));
    }

}
