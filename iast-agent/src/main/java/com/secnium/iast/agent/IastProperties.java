package com.secnium.iast.agent;

import com.secnium.iast.agent.util.LogUtils;

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
    private String engineName;
    private String projectName;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private Integer isAutoCreateProject;

    private String propertiesFilePath;

    public static IastProperties getInstance() {
        if (null == instance) {
            instance = new IastProperties(null);
        }
        return instance;    }

    private IastProperties(String path) {
        try {
            init(path);
        } catch (ClassNotFoundException ignored) {
        }
    }

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

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            iastServerToken = cfg.getProperty("iast.server.token");
        }
        return iastServerToken;
    }


    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("iast.server.url", cfg.getProperty("iast.server.url"));
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
            projectName = System.getProperty("project.name", cfg.getProperty("project.name", "Demo Project"));
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
            proxyPort = Integer.parseInt(System.getProperty("iast.proxy.port", cfg.getProperty("iast.proxy.port", "80")));
        }
        return proxyPort;
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
        return System.getProperty("project.version", cfg.getProperty("project.version","V1.0"));
    }

}
