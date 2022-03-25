package io.dongtai.iast.core.utils;

import io.dongtai.iast.core.utils.json.GsonUtils;
import io.dongtai.log.DongTaiLog;

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
    private Long replayInterval = -1L;
    private String serverUrl;
    private String mode;
    private String serverMode;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String debugFlag;
    private Integer responseLength;

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
                FileInputStream fis = new FileInputStream(propertiesFile);
                cfg.load(fis);
                fis.close();
            }
        } catch (FileNotFoundException e) {
            DongTaiLog.error(e);
        } catch (IOException e) {
            DongTaiLog.error(e);
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
            iastServerToken = System.getProperty("dongtai.server.token", cfg.getProperty("iast.server.token"));
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

    public String getBlackFunctionFilePath() {
        return "com.secnium.iast.resources/blacklistfunc.txt";
    }

    public String getBlackClassFilePath() {
        return "com.secnium.iast.resources/blacklist.txt";
    }

    public String getBlackUrl() {
        return "com.secnium.iast.resources/blackurl.txt";
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

    /**
     * get agent replay http request time
     *
     * @return
     */
    public long getReplayInterval() {
        if (replayInterval == -1L) {
            replayInterval = Long.valueOf(System.getProperty("iast.service.replay.interval",
                    cfg.getProperty("iast.service.replay.interval", "5000")));
        }
        return replayInterval;
    }

    public long getHeartBeatInterval() {
        if (heartBeatInterval == -1L) {
            heartBeatInterval = Long.valueOf(System.getProperty("iast.service.heartbeat.interval",
                    cfg.getProperty("iast.service.heartbeat.interval", "60")));
        }
        return heartBeatInterval;
    }

    /**
     * get OpenAPI Service Address
     *
     * @return OpenAPI Service Address
     */
    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("dongtai.server.url", cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    private String getMode() {
        if (null == mode) {
            mode = System.getProperty("iast.mode", cfg.getProperty("iast.mode", "normal"));
        }
        return mode;
    }

    public boolean isNormalMode() {
        return "normal".equals(getMode());
    }

    /**
     * After version 1.2.0, change the default server mode to local.
     *
     * @return server mode
     */
    private String getServerMode() {
        if (null == serverMode) {
            serverMode = System.getProperty("iast.server.mode", cfg.getProperty("iast.server.mode", "local"));
        }
        return serverMode;
    }

    public boolean isLocal() {
        return "local".equals(getServerMode());
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
            proxyPort = Integer
                    .parseInt(System.getProperty("iast.proxy.port", cfg.getProperty("iast.proxy.port", "80")));
        }
        return proxyPort;
    }

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty("dongtai.debug", "false");
        }
        return debugFlag;
    }

    public boolean isDebug() {
        return "true".equalsIgnoreCase(getDebugFlag());
    }

    public Integer getResponseLength() {
        if(responseLength == null){
            responseLength = Integer.parseInt(System.getProperty("dongtai.response.length", cfg.getProperty("dongtai.response.length","-1")));
        }
        return responseLength;
    }

    /**
     * 获取远端同步的本地配置项
     *
     * @param configKey    配置项
     * @param valueType    值类型
     * @param defaultValue 默认值
     * @param cfg          本地properties配置(为空使用PropertyUtils的配置)
     * @return {@link T} 值类型泛型
     */
    public static <T> T getRemoteSyncLocalConfig(String configKey, Class<T> valueType, T defaultValue, Properties cfg) {
        if (configKey == null || valueType == null) {
            return defaultValue;
        }
        final String config = String.format("iast.remoteSync.%s", configKey);
        final Properties localConfig = cfg != null ? cfg : PropertyUtils.getInstance().cfg;
        final String property = System.getProperty(config, localConfig == null ? null : localConfig.getProperty(config, null));
        if (property == null) {
            return defaultValue;
        }
        try {
            if (valueType.isInstance(valueType)) {
                return valueType.cast(property);
            } else {
                return GsonUtils.castBaseTypeString2Obj(property, valueType);
            }
        } catch (Exception e) {
            DongTaiLog.warn("cast remoteSyncConfig failed!key:{}, valueType:{}, property:{}, err:{}", config, valueType, property, e.getMessage());
            return defaultValue;
        }
    }

    public static <T> T getRemoteSyncLocalConfig(String configKey, Class<T> valueType, T defaultValue) {
        return getRemoteSyncLocalConfig(configKey, valueType, defaultValue, null);
    }

}
