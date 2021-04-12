package com.secnium.iast.core;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.io.File;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropertyUtils {
    private static PropertyUtils instance;
    public PropertiesConfiguration cfg = null;

    private final String propertiesFilePath;

    public static PropertyUtils getInstance(String propertiesFilePath) {
        if (null == instance) {
            instance = new PropertyUtils(propertiesFilePath);
        }
        return instance;
    }

    public static PropertyUtils getInstance() {
        return instance;
    }

    private PropertyUtils(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
        init();
    }


    /**
     * 根据配置文件初始化单例配置类
     */
    private void init() {
        try {
            File propertiesFile = new File(propertiesFilePath);
            if (propertiesFile.exists()) {
                cfg = new PropertiesConfiguration(propertiesFilePath);
                cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getIastName() {
        return cfg.getString("iast.name");
    }

    public String getIastVersion() {
        return cfg.getString("iast.version");
    }

    public String getIastResponseFlagName() {
        return cfg.getString("iast.response.name");
    }

    public String getIastResponseFlagValue() {
        return cfg.getString("iast.response.value");
    }

    public String getIastServerToken() {
        return cfg.getString("iast.server.token");
    }

    @Override
    public String toString() {
        return "[IastName=" + getIastName() +
                ", IastVersion=" + getIastVersion() +
                ", IastResponseName=" + getIastResponseFlagName() +
                "，IastResponseVersion=" + getIastResponseFlagValue() +
                "，IastServerUrl=" + getBaseUrl() +
                "，IastServerToken=" + getIastServerToken() +
                "]";
    }

    public boolean isEnableAllHook() {
        return "true".equals(System.getProperty("iast.allhook.enable", cfg.getString("iast.allhook.enable")));
    }

    public String getSourceFilePath() {
        return "com.secnium.iast.resources/sources.txt";
    }

    public String getBlackFunctionFilePath() {
        return "com.secnium.iast.resources/blacklistfunc.txt";
    }

    public String getBlackClassFilePath() {
        return "com.secnium.iast.resources/blacklist.txt";
    }

    public String getWhiteClassFilePath() {
        return "com.secnium.iast.resources/whitelist.txt";
    }

    public String getBlackExtFilePath() {
        return "com.secnium.iast.resources/blackext.txt";
    }

    public String getDumpClassPath() {
        return System.getProperty("iast.dump.class.path", cfg.getString("iast.dump.class.path"));
    }

    public boolean isEnableDumpClass() {
        return "true".equals(System.getProperty("iast.dump.class.enable", cfg.getString("iast.dump.class.enable")));
    }

    public long getHeartBeatInterval() {
        return cfg.getLong("iast.service.heartbeat.interval", 5 * 60 * 1000);
    }

    public long getReportInterval() {
        return cfg.getLong("iast.service.vulreport.interval", 1000);
    }

    public String getBaseUrl() {
        return System.getProperty("iast.server.url", cfg.getString("iast.server.url"));
    }

    public String getNamespace() {
        return System.getProperty("app.name", cfg.getString("app.name", "IastVulScan"));
    }

    public String getEngineName() {
        return System.getProperty("engine.name", cfg.getString("engine.name", "agent"));
    }

    public String getProjectName() {
        return System.getProperty("project.name", cfg.getString("project.name", "Demo Project"));
    }
}
