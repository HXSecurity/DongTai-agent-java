package com.secnium.iast.agent;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IASTProperties {

    private static IASTProperties instance;
    public PropertiesConfiguration cfg = null;
    private String iastServerToken;
    private String serverUrl;
e
    /**
     * 属性文件路径
     */
    private String propertiesFilePath;

    private IASTProperties(String path) {
        init(path);
    }

    public static IASTProperties getInstance() {
        if (null == instance) {
            instance = new IASTProperties(null);
        }
        return instance;
    }

    public static IASTProperties getInstance(String path) {
        if (null == instance) {
            instance = new IASTProperties(path);
        }
        return instance;
    }

    public String getIastServerToken() {
        if (null == iastServerToken) {
            if (null != cfg) {
                iastServerToken = cfg.getString("iast.server.token", "88d2f0096662335d42580cbd03d8ddea745fdfab");
            } else {
                iastServerToken = "88d2f0096662335d42580cbd03d8ddea745fdfab";
            }
        }
        return iastServerToken;
    }

    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty("iast.server.url", cfg.getString("iast.server.url"));
        }
        return serverUrl;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public String getEngineStatus() {
        return cfg.getString("engine.status");
    }

    public String getEngineName() {
        return cfg.getString("engine.name");
    }

    /**
     * 根据配置文件初始化单例配置类
     */
    public void init(String path) {
        String basePath = null;
        File agentFile;
        File propertiesFile;
        try {
            if (path != null) {
                propertiesFilePath = path;
            } else {
                agentFile = new File(IASTProperties.class.getProtectionDomain().getCodeSource().getLocation().getFile());
                basePath = agentFile.getParentFile().getPath();
                propertiesFilePath = basePath + File.separator + "config" + File.separator + "iast.properties";
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

            InputStream is = IASTProperties.class.getClassLoader().getResourceAsStream("iast.properties");
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

            cfg = new PropertiesConfiguration(propertiesFilePath);
            cfg.setReloadingStrategy(new FileChangedReloadingStrategy());
            System.out.println("[cn.huoxian.dongtai.iast] The engine configuration file is initialized successfully. file is " + propertiesFile.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

}
