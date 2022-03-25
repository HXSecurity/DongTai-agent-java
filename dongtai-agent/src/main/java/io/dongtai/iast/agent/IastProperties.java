package io.dongtai.iast.agent;

import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String engineName;
    private String projectName;
    private String clusterName;
    private String clusterVersion;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private Integer isAutoCreateProject;
    private String debugFlag = null;
    private String isDownloadPackage;

    private String propertiesFilePath;

    private String customCoreJarUrl;
    private String customSpyJarUrl;
    private String customApiJarUrl;

    public static IastProperties getInstance() {
        if (null == instance) {
            instance = new IastProperties();
        }
        return instance;
    }

    private IastProperties() {
        try {
            init();
        } catch (ClassNotFoundException ignored) {
        }
    }

    public void init() throws ClassNotFoundException {
        try {
            propertiesFilePath = System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "iast.properties";
            FileUtils.getResourceToFile("iast.properties", propertiesFilePath);

            InputStream is = IastProperties.class.getClassLoader().getResourceAsStream("iast.properties");
            cfg.load(is);

            DongTaiLog.info("DongTai Config: " + propertiesFilePath);
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            iastServerToken = System.getProperty("dongtai.server.token", cfg.getProperty("iast.server.token"));
        }
        return iastServerToken;
    }


    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("dongtai.server.url", cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getEngineName() {
        if (null == engineName) {
            engineName = System.getProperty("engine.name", cfg.getProperty("engine.name", "agent"));
        }
        return engineName;
    }

    public String getProjectName() {
        if (null == projectName) {
            projectName = System.getProperty(
                    "dongtai.app.name",
                    System.getProperty(
                            "mse.appName",
                            System.getProperty(
                                    "arms.appName",
                                    System.getProperty(
                                            "service.name",
                                            System.getProperty("app.name",
                                                    System.getProperty("project.name",
                                                            cfg.getProperty("project.name", "Demo Project"))
                                            )

                                    )
                            )
                    )
            );
        }
        return projectName;
    }

    public String getClusterName() {
        if (clusterName == null) {
            clusterName = System.getProperty("dongtai.cluster.name", cfg.getProperty("dongtai.cluster.name", ""));
        }
        return clusterName;
    }

    public String getClusterVersion() {
        if (clusterVersion == null) {
            clusterVersion = System.getProperty("dongtai.cluster.version", cfg.getProperty("dongtai.cluster.version", ""));
        }
        return clusterVersion;
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

    public Integer isAutoCreateProject() {
        if (null == isAutoCreateProject) {
            String result = System.getProperty(
                    "dongtai.app.create",
                    System.getProperty(
                            "project.create",
                            cfg.getProperty("project.create", "false")
                    )
            );
            if ("false".equals(result)) {
                isAutoCreateProject = 0;
            } else if ("true".equals(result)) {
                isAutoCreateProject = 1;
            }
        }
        return isAutoCreateProject;
    }

    public String getProjectVersion() {
        return System.getProperty(
                "dongtai.app.version",
                System.getProperty(
                        "project.version",
                        cfg.getProperty("project.version", "V1.0")
                )
        );
    }

    public String getCustomSpyJarUrl() {
        if (null == customSpyJarUrl) {
            customSpyJarUrl = System.getProperty("iast.jar.spy.url", cfg.getProperty("iast.jar.spy.url", ""));
        }
        return customSpyJarUrl;
    }

    public String getCustomCoreJarUrl() {
        if (null == customCoreJarUrl) {
            customCoreJarUrl = System.getProperty("iast.jar.core.url", cfg.getProperty("iast.jar.core.url", ""));
        }
        return customCoreJarUrl;
    }

    public String getCustomApiJarUrl() {
        if (null == customApiJarUrl) {
            customApiJarUrl = System.getProperty("iast.jar.api.url", cfg.getProperty("iast.jar.api.url", ""));
        }
        return customApiJarUrl;
    }

    public String getIsDownloadPackage() {
        if (null == isDownloadPackage) {
            isDownloadPackage = System.getProperty("dongtai.server.package", cfg.getProperty("dongtai.server.package", "true"));
        }
        return isDownloadPackage;
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

}
