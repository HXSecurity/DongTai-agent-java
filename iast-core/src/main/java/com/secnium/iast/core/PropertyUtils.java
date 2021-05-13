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
    private String iastName;
    private String iastVersion;
    private String iastResponseName;
    private String iastResponseValue;
    private String iastServerToken;
    private String allHookState;
    private String dumpClassState;
    private String iastDumpPath;
    private Long heartBeatInterval = -1L;
    private Long reportInterval = -1L;
    private String serverUrl;
    private String namespace;
    private String engineName;
    private String projectName;
    private String mode;
    private String serverMode;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;

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
        if (null == iastName) {
            iastName = cfg.getString("iast.name");
        }
        return iastName;
    }

    public String getIastVersion() {
        if (null == iastVersion) {
            iastVersion = cfg.getString("iast.version");
        }
        return iastVersion;
    }

    public String getIastResponseFlagName() {
        if (null == iastResponseName) {
            iastResponseName = cfg.getString("iast.response.name");
        }
        return iastResponseName;
    }

    public String getIastResponseFlagValue() {
        if (null == iastResponseValue) {
            iastResponseValue = cfg.getString("iast.response.value");
        }
        return iastResponseValue;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            iastServerToken = cfg.getString("iast.server.token");
        }
        return iastServerToken;
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

    private String getAllHookState() {
        if (null == allHookState) {
            allHookState = System.getProperty("iast.allhook.enable", cfg.getString("iast.allhook.enable"));
        }
        return allHookState;
    }

    public boolean isEnableAllHook() {
        return "true".equals(getAllHookState());
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
        if (null == iastDumpPath) {
            iastDumpPath = System.getProperty("iast.dump.class.path", cfg.getString("iast.dump.class.path"));
        }
        return iastDumpPath;
    }

    private String getDumpClassState() {
        if (null == dumpClassState) {
            dumpClassState = System.getProperty("iast.dump.class.enable", cfg.getString("iast.dump.class.enable"));
        }
        return dumpClassState;
    }

    public boolean isEnableDumpClass() {
        return "true".equals(getDumpClassState());
    }

    public long getHeartBeatInterval() {
        if (heartBeatInterval == -1L) {
            heartBeatInterval = cfg.getLong("iast.service.heartbeat.interval", 5 * 60 * 1000);
        }
        return heartBeatInterval;
    }

    public long getReportInterval() {
        if (reportInterval == -1L) {
            reportInterval = cfg.getLong("iast.service.vulreport.interval", 1000);
        }
        return reportInterval;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("iast.server.url", cfg.getString("iast.server.url"));
        }
        return serverUrl;
    }

    public String getNamespace() {
        if (null == namespace) {
            namespace = System.getProperty("app.name", cfg.getString("app.name", "IastVulScan"));
        }
        return namespace;
    }

    public String getEngineName() {
        if (null == engineName) {
            engineName = System.getProperty("engine.name", cfg.getString("engine.name", "agent"));
        }
        return engineName;
    }

    public String getProjectName() {
        if (null == projectName) {
            projectName = System.getProperty("project.name", cfg.getString("project.name", "Demo Project"));
        }
        return projectName;
    }

    private String getMode() {
        if (null == mode) {
            mode = System.getProperty("iast.mode", cfg.getString("iast.mode", "normal"));
        }
        return mode;
    }

    public boolean isHunterMode() {
        return "hunter".equals(getMode());
    }

    public boolean isNormalMode() {
        return "normal".equals(getMode());
    }

    private String getServerMode() {
        if (null == serverMode) {
            serverMode = System.getProperty("iast.server.mode", "remote");
        }
        return serverMode;
    }

    public boolean isLocal() {
        return "local".equals(getServerMode());
    }

    public boolean isRemote() {
        return "remote".equals(getServerMode());
    }

    private String getProxyEnableStatus() {
        if (null == proxyEnableStatus) {
            proxyEnableStatus = System.getProperty("iast.proxy.enable", cfg.getString("iast.proxy.enable", "false"));
        }
        return proxyEnableStatus;
    }

    public boolean isProxyEnable() {
        return "true".equalsIgnoreCase(getProxyEnableStatus());
    }

    public String getProxyHost() {
        if (null == proxyHost) {
            proxyHost = System.getProperty("iast.proxy.host", cfg.getString("iast.proxy.host", "false"));
        }
        return proxyHost;
    }

    public int getProxyPort() {
        if (-1 == proxyPort) {
            proxyPort = Integer.parseInt(System.getProperty("iast.proxy.port", cfg.getString("iast.proxy.port", "80")));
        }
        return proxyPort;
    }
}
