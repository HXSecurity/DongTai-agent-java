package com.secnium.iast.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropertyUtils {
    private static PropertyUtils instance;
    public Properties cfg = null;
    private String iastName;
    private String iastResponseName;
    private String iastResponseValue;
    private String iastServerToken;
    private String allHookState;
    private String dumpClassState;
    private String iastDumpPath;
    private Long heartBeatInterval = -1L;
    private Long reportInterval = -1L;
    private Long replayInterval = -1L;
    private String serverUrl;
    private String namespace;
    private String engineName;
    private String projectName;
    private String mode;
    private String serverMode;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String debugFlag;
    private Integer isAutoCreateProject;

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
                cfg = new Properties();
                cfg.load(new FileInputStream(propertiesFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIastName() {
        if (null == iastName) {
            iastName = cfg.getProperty("iast.name");
        }
        return iastName;
    }

    public String getIastResponseFlagName() {
        if (null == iastResponseName) {
            iastResponseName = cfg.getProperty("iast.response.name");
        }
        return iastResponseName;
    }

    public String getIastResponseFlagValue() {
        if (null == iastResponseValue) {
            iastResponseValue = cfg.getProperty("iast.response.value");
        }
        return iastResponseValue;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            iastServerToken = cfg.getProperty("iast.server.token");
        }
        return iastServerToken;
    }

    @Override
    public String toString() {
        return "[IastName=" + getIastName() +
                ", IastResponseName=" + getIastResponseFlagName() +
                "，IastResponseVersion=" + getIastResponseFlagValue() +
                "，IastServerUrl=" + getBaseUrl() +
                "，IastServerToken=" + getIastServerToken() +
                "]";
    }

    private String getAllHookState() {
        if (null == allHookState) {
            allHookState = System.getProperty("iast.allhook.enable", cfg.getProperty("iast.allhook.enable"));
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
            iastDumpPath = System.getProperty("iast.dump.class.path", cfg.getProperty("iast.dump.class.path"));
        }
        return iastDumpPath;
    }

    private String getDumpClassState() {
        if (null == dumpClassState) {
            dumpClassState = System.getProperty("iast.dump.class.enable", cfg.getProperty("iast.dump.class.enable"));
        }
        return dumpClassState;
    }

    public boolean isEnableDumpClass() {
        return "true".equals(getDumpClassState());
    }

    public long getReplayInterval() {
        if (replayInterval == -1L) {
            replayInterval = Long.valueOf(System.getProperty("iast.service.replay.interval", cfg.getProperty("iast.service.replay.interval", "5000")));
        }
        return replayInterval;
    }

    public long getReportInterval() {
        if (reportInterval == -1L) {
            reportInterval = Long.valueOf(System.getProperty("iast.service.report.interval", cfg.getProperty("iast.service.report.interval", "60000")));
        }
        return reportInterval;
    }

    public long getHeartBeatInterval() {
        if (heartBeatInterval == -1L) {
            heartBeatInterval = Long.valueOf(System.getProperty("iast.service.heartbeat.interval", cfg.getProperty("iast.service.heartbeat.interval", "30")));
        }
        return heartBeatInterval;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("iast.server.url", cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getNamespace() {
        if (null == namespace) {
            namespace = System.getProperty("app.name", cfg.getProperty("app.name", "IastVulScan"));
        }
        return namespace;
    }

    public String getEngineName() {
        if (null == engineName) {
            engineName = System.getProperty("engine.name", cfg.getProperty("engine.name", "agent"));
        }
        return engineName;
    }

    public String getProjectName() {
        if (null == projectName) {
            projectName = System.getProperty("project.name", cfg.getProperty("project.name", "Demo Project"));
        }
        return projectName;
    }

    private String getMode() {
        if (null == mode) {
            mode = System.getProperty("iast.mode", cfg.getProperty("iast.mode", "normal"));
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

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty("debug", "false");
        }
        return debugFlag;
    }

    public boolean isDebug() {
        return "true".equalsIgnoreCase(debugFlag);
    }

    public Integer isAutoCreateProject() {
        if (null == isAutoCreateProject) {
            String result = System.getProperty("project.create", cfg.getProperty("project.create", "false"));
            if (result.equals("false")) {
                isAutoCreateProject = 0;
            } else if (result.equals("true")) {
                isAutoCreateProject = 1;
            }
        }
        return isAutoCreateProject;
    }


    public String getProjectVersion() {
        return System.getProperty("project.version", cfg.getProperty("project.version", "V1.0"));
    }

    public Integer getResponseLength() {
        return Integer.parseInt(System.getProperty("response.length", cfg.getProperty("response.length")));
    }
}
