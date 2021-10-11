package com.secnium.iast.agent;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {

    private static IastProperties instance;
    public Properties cfg = new Properties();
    private String iastServerToken;
    private String serverUrl;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private int delayTime = -1;

    /**
     * 属性文件路径
     */
    private String propertiesFilePath;

    private IastProperties(String path) {
        try {
            init(path);
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static IastProperties getInstance() {
        if (null == instance) {
            instance = new IastProperties(null);
        }
        return instance;
    }

    public static IastProperties getInstance(String path) {
        if (null == instance) {
            instance = new IastProperties(path);
        }
        return instance;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            if (null != cfg) {
                iastServerToken = cfg.getProperty("iast.server.token", "88d2f0096662335d42580cbd03d8ddea745fdfab");
            } else {
                iastServerToken = "88d2f0096662335d42580cbd03d8ddea745fdfab";
            }
        }
        return iastServerToken;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("iast.server.url", cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getEngineStatus() {
        return cfg.getProperty("engine.status");
    }

    public String getEngineName() {
        return cfg.getProperty("engine.name");
    }

    private String getProxyEnableStatus() {
        if (null == proxyEnableStatus) {
            proxyEnableStatus = System.getProperty("iast.proxy.enable", cfg.getProperty("iast.proxy.enable", "false"));
        }
        return proxyEnableStatus;
    }

    public boolean isProxyEnable() {
        return "true".equalsIgnoreCase(getProxyEnableStatus());
    }

    public String getProxyHost() {
        if (null == proxyHost) {
            proxyHost = System.getProperty("iast.proxy.host", cfg.getProperty("iast.proxy.host", "false"));
        }
        return proxyHost;
    }

    public int getProxyPort() {
        if (-1 == proxyPort) {
            proxyPort = Integer.parseInt(System.getProperty("iast.proxy.port", cfg.getProperty("iast.proxy.port", "80")));
        }
        return proxyPort;
    }

    public int getDelayTime() {
        if (-1 == delayTime) {
            delayTime = Integer.parseInt(System.getProperty("iast.engine.delay.time", "10"));
        }
        return delayTime;
    }

    /**
     * 根据配置文件初始化单例配置类
     */
    public void init(String path) throws ClassNotFoundException {
        String basePath = null;
        File agentFile;
        File propertiesFile;
        try {
            if (path != null) {
                propertiesFilePath = path;
            } else {
                agentFile = new File(IastProperties.class.getProtectionDomain().getCodeSource().getLocation().getFile());
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

            LogUtils.info("The engine configuration file is initialized successfully. file is " + propertiesFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
