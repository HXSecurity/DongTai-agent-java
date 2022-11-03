package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.PropertyConstant;

import java.io.*;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {
    public final static Map<String, String> ATTACH_ARG_MAP = new HashMap<String, String>() {{
        put("debug", PropertyConstant.PROPERTY_DEBUG);
        put("app_create", PropertyConstant.PROPERTY_APP_CREATE);
        put("app_name", PropertyConstant.PROPERTY_APP_NAME);
        put("app_version", PropertyConstant.PROPERTY_APP_VERSION);
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
    }};

    private static IastProperties instance;
    public Properties cfg = new Properties();
    private String propertiesFilePath;

    private String debugFlag = null;
    private Integer isAutoCreateProject;
    private String projectName;
    private String clusterName;
    private String clusterVersion;
    private String serverUrl;
    private String serverToken;
    private String isDownloadPackage;
    private String engineName;
    private String logPort;
    private String logAddress;
    private Boolean logDisableCollector;

    private Integer delayTime;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String fallbackVersion;
    private String customCoreJarUrl;
    private String customSpyJarUrl;
    private String customApiJarUrl;
    private String tmpDir;

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
            init();
        } catch (ClassNotFoundException e) {
            System.out.println("IastProperties initialization failed: " + e.getMessage());
        }
    }

    private void initTmpDir() {
        StringBuilder dir = new StringBuilder();
        String sysTmpDir = System.getProperty("java.io.tmpdir");
        if (sysTmpDir == null) {
            sysTmpDir = File.separator + "tmp";
        }
        dir.append(sysTmpDir).append(File.separator)
                .append("dongtai-").append(System.getProperty("user.name")).append(File.separator)
                .append(AgentConstant.VERSION_VALUE).append(File.separator);

        this.tmpDir = dir.toString();
        System.setProperty("java.io.tmpdir.dongtai", this.tmpDir);
    }

    public String getTmpDir() {
        if (this.tmpDir.isEmpty()) {
            initTmpDir();
        }
        return this.tmpDir;
    }

    public void init() throws ClassNotFoundException {
        try {
            initTmpDir();
            propertiesFilePath = getTmpDir() + "iast.properties";
            FileUtils.getResourceToFile("iast.properties", propertiesFilePath);

            InputStream is = IastProperties.class.getClassLoader().getResourceAsStream("iast.properties");
            cfg.load(is);

            System.out.println("[io.dongtai.iast.agent] DongTai Config: " + propertiesFilePath);
        } catch (IOException e) {
            System.out.println("[io.dongtai.iast.agent] read iast.properties failed: " + e.getMessage());
        }
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

    public Integer isAutoCreateProject() {
        if (null == isAutoCreateProject) {
            String result = System.getProperty(PropertyConstant.PROPERTY_APP_CREATE,
                    System.getProperty("project.create", cfg.getProperty("project.create", "false"))
            );
            if ("true".equalsIgnoreCase(result)) {
                isAutoCreateProject = 1;
            } else {
                isAutoCreateProject = 0;
            }
        }
        return isAutoCreateProject;
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
        return System.getProperty(PropertyConstant.PROPERTY_APP_VERSION,
                System.getProperty("project.version", cfg.getProperty("project.version", "V1.0"))
        );
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
                    cfg.getProperty(PropertyConstant.PROPERTY_SERVER_PACKAGE, "true"));
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

    public String getFallbackVersion() {
        if (fallbackVersion == null) {
            fallbackVersion = System.getProperty(PropertyConstant.PROPERTY_FALLBACK_VERSION, "v2");
        }
        return fallbackVersion;
    }
}
