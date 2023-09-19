package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.agent.util.GsonUtils;
import io.dongtai.iast.common.constants.PropertyConstant;
import io.dongtai.iast.common.constants.Version;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {
    public final static Map<String, String> ATTACH_ARG_MAP = new HashMap<String, String>() {{
        put("debug", PropertyConstant.PROPERTY_DEBUG);
        put("app_name", PropertyConstant.PROPERTY_APP_NAME);
        put("app_version", PropertyConstant.PROPERTY_APP_VERSION);
        put("app_template", PropertyConstant.PROPERTY_APP_TEMPLATE);
        put("engine_name", PropertyConstant.PROPERTY_ENGINE_NAME);
        put("cluster_name", PropertyConstant.PROPERTY_CLUSTER_NAME);
        put("cluster_version", PropertyConstant.PROPERTY_CLUSTER_VERSION);
        put("dongtai_server", PropertyConstant.PROPERTY_SERVER_URL);
        put("dongtai_token", PropertyConstant.PROPERTY_SERVER_TOKEN);
        put("server_package", PropertyConstant.PROPERTY_SERVER_PACKAGE);
        put("policy_path", PropertyConstant.PROPERTY_POLICY_PATH);
        put("log", PropertyConstant.PROPERTY_LOG);
        put("log_level", PropertyConstant.PROPERTY_LOG_LEVEL);
        put("log_path", PropertyConstant.PROPERTY_LOG_PATH);
        put("log_disable_collector", PropertyConstant.PROPERTY_LOG_DISABLE_COLLECTOR);
        put("uuid_path", PropertyConstant.PROPERTY_UUID_PATH);
        put("disabled_plugins", PropertyConstant.PROPERTY_DISABLED_PLUGINS);
        put("disabled_features", PropertyConstant.PROPERTY_DISABLED_FEATURES);
        put("pool_capacity", PropertyConstant.PROPERTY_POOL_CAPACITY);
        put("pool_size", PropertyConstant.PROPERTY_POOL_SIZE);
        put("pool_max_size", PropertyConstant.PROPERTY_POOL_MAX_SIZE);
        put("pool_keepalive", PropertyConstant.PROPERTY_POOL_KEEPALIVE);
    }};

    private static IastProperties instance;
    public Properties cfg = new Properties();
    private String propertiesFilePath;

    private String debugFlag = null;
    private Integer isAutoCreateProject;
    private String projectName;
    private String projectVersion;
    private Integer projectTemplate;
    private String clusterName;
    private String clusterVersion;
    private String serverUrl;
    private String serverToken;
    private String isDownloadPackage;
    private String engineName;
    private String logPort;
    private String logAddress;
    private Boolean logDisableCollector;
    private String uuidPath;

    private Integer delayTime;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String customCoreJarUrl;
    private String customSpyJarUrl;
    private String customApiJarUrl;
    private static String TMP_DIR;

    public static IastProperties getInstance() {
        if (null == instance) {
            instance = new IastProperties();
        }
        return instance;
    }

    public static void setInstance(IastProperties instance) {
        IastProperties.instance = instance;
    }

    private IastProperties() {
        try {
            propertiesFilePath = getTmpDir() + "iast.properties";
            FileUtils.getResourceToFile("iast.properties", propertiesFilePath);

            InputStream is = IastProperties.class.getClassLoader().getResourceAsStream("iast.properties");
            cfg.load(is);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_PROPERTIES_INITIALIZE_FAILED, e);
        }
    }

    public static String initTmpDir() {
        if (TMP_DIR != null && !TMP_DIR.isEmpty()) {
            return TMP_DIR;
        }
        StringBuilder dir = new StringBuilder();
        String sysTmpDir = System.getProperty("java.io.tmpdir");
        if (sysTmpDir == null) {
            sysTmpDir = File.separator + "tmp";
        }
        dir.append(sysTmpDir).append(File.separator)
                .append("dongtai-").append(System.getProperty("user.name")).append(File.separator)
                .append(Version.VERSION).append(File.separator);

        TMP_DIR = dir.toString();
        System.setProperty("java.io.tmpdir.dongtai", TMP_DIR);
        return TMP_DIR;
    }

    public String getTmpDir() {
        if (TMP_DIR.isEmpty()) {
            initTmpDir();
        }
        return TMP_DIR;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty(PropertyConstant.PROPERTY_DEBUG, "false");
        }
        return debugFlag;
    }

    public boolean isDebug() {
        return "true".equalsIgnoreCase(getDebugFlag());
    }

    public String getProjectName() {
        if (null == projectName) {
            String[] names = new String[]{
                    PropertyConstant.PROPERTY_APP_NAME, "mse.appName", "arms.appName",
                    "service.name", "app.name", "project.name",
            };
            for (String name : names) {
                projectName = System.getProperty(name);
                if (projectName != null && !projectName.isEmpty()) {
                    return projectName;
                }
            }
            projectName = cfg.getProperty("project.name", "Demo Project");
        }
        return projectName;
    }

    public String getProjectVersion() {
        if (null == projectVersion) {
            projectVersion = System.getProperty(PropertyConstant.PROPERTY_APP_VERSION,
                    System.getProperty("project.version", cfg.getProperty("project.version", "V1.0"))
            );
        }
        return projectVersion;
    }

    public Integer getProjectTemplate() {
        if (null == projectTemplate) {
            try {
                projectTemplate = Integer.parseInt(System.getProperty(PropertyConstant.PROPERTY_APP_TEMPLATE,
                        cfg.getProperty(PropertyConstant.PROPERTY_APP_TEMPLATE, "0"))
                );
            } catch (NumberFormatException e) {
                projectTemplate = 0;
            }
        }
        return projectTemplate;
    }

    public String getEngineName() {
        if (null == engineName) {
            engineName = System.getProperty(PropertyConstant.PROPERTY_ENGINE_NAME,
                    cfg.getProperty("engine.name", "agent"));
        }
        return engineName;
    }

    public String getClusterName() {
        if (clusterName == null) {
            clusterName = System.getProperty(PropertyConstant.PROPERTY_CLUSTER_NAME,
                    cfg.getProperty(PropertyConstant.PROPERTY_CLUSTER_NAME, ""));
        }
        return clusterName;
    }

    public String getClusterVersion() {
        if (clusterVersion == null) {
            clusterVersion = System.getProperty(PropertyConstant.PROPERTY_CLUSTER_VERSION,
                    cfg.getProperty(PropertyConstant.PROPERTY_CLUSTER_VERSION, ""));
        }
        return clusterVersion;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty(PropertyConstant.PROPERTY_SERVER_URL,
                    cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getServerToken() {
        if (null == serverToken) {
            serverToken = System.getProperty(PropertyConstant.PROPERTY_SERVER_TOKEN,
                    cfg.getProperty("iast.server.token"));
        }
        return serverToken;
    }

    public String getIsDownloadPackage() {
        if (null == isDownloadPackage) {
            isDownloadPackage = System.getProperty(PropertyConstant.PROPERTY_SERVER_PACKAGE,
                    cfg.getProperty(PropertyConstant.PROPERTY_SERVER_PACKAGE, "false"));
        }
        return isDownloadPackage;
    }

    public Boolean getLogDisableCollector() {
        if (logDisableCollector == null) {
            String disable = System.getProperty(PropertyConstant.PROPERTY_LOG_DISABLE_COLLECTOR,
                    cfg.getProperty(PropertyConstant.PROPERTY_LOG_DISABLE_COLLECTOR, "false"));
            logDisableCollector = "true".equalsIgnoreCase(disable);
        }
        return logDisableCollector;
    }

    public Integer getDelayTime() {
        if (delayTime == null) {
            delayTime = Integer.parseInt(System.getProperty(PropertyConstant.PROPERTY_ENGINE_DELAY_TIME,
                    cfg.getProperty(PropertyConstant.PROPERTY_ENGINE_DELAY_TIME, "0")));
        }
        return delayTime;
    }

    private String getProxyEnableStatus() {
        if (null == proxyEnableStatus) {
            proxyEnableStatus = System.getProperty(PropertyConstant.PROPERTY_PROXY_ENABLE,
                    cfg.getProperty(PropertyConstant.PROPERTY_PROXY_ENABLE, "false"));
        }
        return proxyEnableStatus;
    }

    public boolean isProxyEnable() {
        return "true".equalsIgnoreCase(getProxyEnableStatus());
    }

    public String getProxyHost() {
        if (null == proxyHost) {
            proxyHost = System.getProperty(PropertyConstant.PROPERTY_PROXY_HOST,
                    cfg.getProperty(PropertyConstant.PROPERTY_PROXY_HOST, ""));
        }
        return proxyHost;
    }

    public int getProxyPort() {
        if (-1 == proxyPort) {
            proxyPort = Integer.parseInt(System.getProperty(PropertyConstant.PROPERTY_PROXY_PORT,
                    cfg.getProperty(PropertyConstant.PROPERTY_PROXY_PORT, "")));
        }
        return proxyPort;
    }

    public String getCustomSpyJarUrl() {
        if (null == customSpyJarUrl) {
            customSpyJarUrl = System.getProperty(PropertyConstant.PROPERTY_JAR_SPY_URL,
                    cfg.getProperty(PropertyConstant.PROPERTY_JAR_SPY_URL, ""));
        }
        return customSpyJarUrl;
    }

    public String getCustomCoreJarUrl() {
        if (null == customCoreJarUrl) {
            customCoreJarUrl = System.getProperty(PropertyConstant.PROPERTY_JAR_CORE_URL,
                    cfg.getProperty(PropertyConstant.PROPERTY_JAR_CORE_URL, ""));
        }
        return customCoreJarUrl;
    }

    public String getCustomApiJarUrl() {
        if (null == customApiJarUrl) {
            customApiJarUrl = System.getProperty(PropertyConstant.PROPERTY_JAR_API_URL,
                    cfg.getProperty(PropertyConstant.PROPERTY_JAR_API_URL, ""));
        }
        return customApiJarUrl;
    }

    public String getLogAddress() {
        if (logAddress == null) {
            logAddress = System.getProperty(PropertyConstant.PROPERTY_LOG_ADDRESS, "");
        }
        return logAddress;
    }

    public String getLogPort() {
        if (logPort == null) {
            logPort = System.getProperty(PropertyConstant.PROPERTY_LOG_PORT, "");
        }
        return logPort;
    }

    public String getUUIDPath() {
        if (uuidPath == null) {
            uuidPath = System.getProperty(PropertyConstant.PROPERTY_UUID_PATH, "");
        }
        return uuidPath;
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
    public <T> T getRemoteFallbackConfig(String configKey, Class<T> valueType, T defaultValue, Properties cfg) {
        if (configKey == null || valueType == null) {
            return defaultValue;
        }
        final String config = String.format("iast.remoteSync.%s", configKey);
        final Properties localConfig = cfg != null ? cfg : this.cfg;
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
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_PROCESS_REMOTE_FALLBACK_CONFIG_FAILED, config, valueType, e);
            return defaultValue;
        }
    }

    public <T> T getRemoteFallbackConfig(String configKey, Class<T> valueType, T defaultValue) {
        return getRemoteFallbackConfig(configKey, valueType, defaultValue, null);
    }
}
