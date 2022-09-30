package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.log.DongTaiLog;

import java.io.*;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastProperties {
    public final static String PROPERTY_DEBUG = "dongtai.debug";
    public final static String PROPERTY_APP_CREATE = "dongtai.app.create";
    public final static String PROPERTY_APP_NAME = "dongtai.app.name";
    public final static String PROPERTY_APP_VERSION = "dongtai.app.version";
    public final static String PROPERTY_ENGINE_NAME = "dongtai.engine.name";
    public final static String PROPERTY_CLUSTER_NAME = "dongtai.cluster.name";
    public final static String PROPERTY_CLUSTER_VERSION = "dongtai.cluster.version";
    public final static String PROPERTY_SERVER_URL = "dongtai.server.url";
    public final static String PROPERTY_SERVER_TOKEN = "dongtai.server.token";
    public final static String PROPERTY_SERVER_PACKAGE = "dongtai.server.package";
    public final static String PROPERTY_LOG = "dongtai.log";
    public final static String PROPERTY_LOG_LEVEL = "dongtai.log.level";
    public final static String PROPERTY_LOG_PATH = "dongtai.log.path";
    public final static String PROPERTY_LOG_DISABLE_COLLECTOR = "dongtai.log.disable-collector";
    public final static String PROPERTY_ENGINE_DELAY_TIME = "iast.engine.delay.time";
    public final static String PROPERTY_PROXY_ENABLE = "iast.proxy.enable";
    public final static String PROPERTY_PROXY_HOST = "iast.proxy.host";
    public final static String PROPERTY_PROXY_PORT = "iast.proxy.port";
    public final static String PROPERTY_JAR_SPY_URL = "iast.jar.spy.url";
    public final static String PROPERTY_JAR_CORE_URL = "iast.jar.core.url";
    public final static String PROPERTY_JAR_API_URL = "iast.jar.api.url";
    public final static String PROPERTY_LOG_ADDRESS = "dongtai.log.address";
    public final static String PROPERTY_LOG_PORT = "dongtai.log.port";
    public final static String PROPERTY_FALLBACK_VERSION = "dongtai.fallback.version";

    public final static Map<String, String> ATTACH_ARG_MAP = new HashMap<String, String>() {{
        put("debug", IastProperties.PROPERTY_DEBUG);
        put("app_create", IastProperties.PROPERTY_APP_CREATE);
        put("app_name", IastProperties.PROPERTY_APP_NAME);
        put("app_version", IastProperties.PROPERTY_APP_VERSION);
        put("engine_name", IastProperties.PROPERTY_ENGINE_NAME);
        put("cluster_name", IastProperties.PROPERTY_CLUSTER_NAME);
        put("cluster_version", IastProperties.PROPERTY_CLUSTER_VERSION);
        put("dongtai_server", IastProperties.PROPERTY_SERVER_URL);
        put("dongtai_token", IastProperties.PROPERTY_SERVER_TOKEN);
        put("server_package", IastProperties.PROPERTY_SERVER_PACKAGE);
        put("log", IastProperties.PROPERTY_LOG);
        put("log_level", IastProperties.PROPERTY_LOG_LEVEL);
        put("log_path", IastProperties.PROPERTY_LOG_PATH);
        put("log_disable_collector", IastProperties.PROPERTY_LOG_DISABLE_COLLECTOR);
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
            DongTaiLog.error("IastProperties initialization failed", e);
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

            DongTaiLog.info("DongTai Config: " + propertiesFilePath);
        } catch (IOException e) {
            DongTaiLog.error("read iast.properties failed", e);
        }
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty(PROPERTY_DEBUG, "false");
        }
        return debugFlag;
    }

    public boolean isDebug() {
        return "true".equalsIgnoreCase(getDebugFlag());
    }

    public Integer isAutoCreateProject() {
        if (null == isAutoCreateProject) {
            String result = System.getProperty(PROPERTY_APP_CREATE,
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
                    PROPERTY_APP_NAME, "mse.appName", "arms.appName", "service.name", "app.name", "project.name",
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
        return System.getProperty(PROPERTY_APP_VERSION,
                System.getProperty("project.version", cfg.getProperty("project.version", "V1.0"))
        );
    }

    public String getEngineName() {
        if (null == engineName) {
            engineName = System.getProperty(PROPERTY_ENGINE_NAME, cfg.getProperty("engine.name", "agent"));
        }
        return engineName;
    }

    public String getClusterName() {
        if (clusterName == null) {
            clusterName = System.getProperty(PROPERTY_CLUSTER_NAME, cfg.getProperty(PROPERTY_CLUSTER_NAME, ""));
        }
        return clusterName;
    }

    public String getClusterVersion() {
        if (clusterVersion == null) {
            clusterVersion = System.getProperty(PROPERTY_CLUSTER_VERSION, cfg.getProperty(PROPERTY_CLUSTER_VERSION, ""));
        }
        return clusterVersion;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty(PROPERTY_SERVER_URL, cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getServerToken() {
        if (null == serverToken) {
            serverToken = System.getProperty(PROPERTY_SERVER_TOKEN, cfg.getProperty("iast.server.token"));
        }
        return serverToken;
    }

    public String getIsDownloadPackage() {
        if (null == isDownloadPackage) {
            isDownloadPackage = System.getProperty(PROPERTY_SERVER_PACKAGE, cfg.getProperty(PROPERTY_SERVER_PACKAGE, "true"));
        }
        return isDownloadPackage;
    }

    public Boolean getLogDisableCollector() {
        if (logDisableCollector == null) {
            String disable = System.getProperty(PROPERTY_LOG_DISABLE_COLLECTOR,
                    cfg.getProperty(PROPERTY_LOG_DISABLE_COLLECTOR, "false"));
            logDisableCollector = "true".equalsIgnoreCase(disable);
        }
        return logDisableCollector;
    }

    public Integer getDelayTime() {
        if (delayTime == null) {
            delayTime = Integer.parseInt(System.getProperty(PROPERTY_ENGINE_DELAY_TIME,
                    cfg.getProperty(PROPERTY_ENGINE_DELAY_TIME, "0")));
        }
        return delayTime;
    }

    private String getProxyEnableStatus() {
        if (null == proxyEnableStatus) {
            proxyEnableStatus = System.getProperty(PROPERTY_PROXY_ENABLE, cfg.getProperty(PROPERTY_PROXY_ENABLE, "false"));
        }
        return proxyEnableStatus;
    }

    public boolean isProxyEnable() {
        return "true".equalsIgnoreCase(getProxyEnableStatus());
    }

    public String getProxyHost() {
        if (null == proxyHost) {
            proxyHost = System.getProperty(PROPERTY_PROXY_HOST, cfg.getProperty(PROPERTY_PROXY_HOST, ""));
        }
        return proxyHost;
    }

    public int getProxyPort() {
        if (-1 == proxyPort) {
            proxyPort = Integer.parseInt(System.getProperty(PROPERTY_PROXY_PORT,
                    cfg.getProperty(PROPERTY_PROXY_PORT, "")));
        }
        return proxyPort;
    }

    public String getCustomSpyJarUrl() {
        if (null == customSpyJarUrl) {
            customSpyJarUrl = System.getProperty(PROPERTY_JAR_SPY_URL, cfg.getProperty(PROPERTY_JAR_SPY_URL, ""));
        }
        return customSpyJarUrl;
    }

    public String getCustomCoreJarUrl() {
        if (null == customCoreJarUrl) {
            customCoreJarUrl = System.getProperty(PROPERTY_JAR_CORE_URL, cfg.getProperty(PROPERTY_JAR_CORE_URL, ""));
        }
        return customCoreJarUrl;
    }

    public String getCustomApiJarUrl() {
        if (null == customApiJarUrl) {
            customApiJarUrl = System.getProperty(PROPERTY_JAR_API_URL, cfg.getProperty(PROPERTY_JAR_API_URL, ""));
        }
        return customApiJarUrl;
    }

    public String getLogAddress() {
        if (logAddress == null) {
            logAddress = System.getProperty(PROPERTY_LOG_ADDRESS, "");
        }
        return logAddress;
    }

    public String getLogPort() {
        if (logPort == null) {
            logPort = System.getProperty(PROPERTY_LOG_PORT, "");
        }
        return logPort;
    }

    public String getFallbackVersion() {
        if (fallbackVersion == null) {
            fallbackVersion = System.getProperty(PROPERTY_FALLBACK_VERSION, "v2");
        }
        return fallbackVersion;
    }
}
