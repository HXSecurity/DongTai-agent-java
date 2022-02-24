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
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private Integer isAutoCreateProject;
    private String debugFlag = null;

    private String propertiesFilePath;

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
            propertiesFilePath = System.getProperty("java.io.tmpdir") + File.separator + "iast" + File.separator + "iast.properties";
            FileUtils.getResourceToFile("iast.properties", propertiesFilePath);

            InputStream is = IastProperties.class.getClassLoader().getResourceAsStream("iast.properties");
            cfg.load(is);

            DongTaiLog.info("DongTai Config: " + propertiesFilePath);
        } catch (IOException e) {
            e.printStackTrace();
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
