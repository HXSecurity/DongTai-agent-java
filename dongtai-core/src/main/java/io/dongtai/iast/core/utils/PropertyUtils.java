package io.dongtai.iast.core.utils;

import io.dongtai.iast.common.constants.PropertyConstant;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropertyUtils {

    private static PropertyUtils instance;
    public Properties cfg = null;
    private String iastName;
    private String iastServerToken;
    private String dumpClassState;
    private String iastDumpPath;
    private Long heartBeatInterval = -1L;
    private String serverUrl;
    private String serverMode;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String debugFlag;
    private Integer responseLength;
    private String policyPath;
    private static List<String> disabledFeatureList;
    private static Boolean isDisabledCustomModel;

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

    public static void clear() {
        instance = null;
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
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("ENGINE_PROPERTIES_INITIALIZE_FAILED"), e);
        }
    }

    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir.dongtai");
    }

    public String getIastName() {
        if (null == iastName) {
            iastName = cfg.getProperty("iast.name");
        }
        return iastName;
    }

    /**
     * get OpenAPI Service Address
     *
     * @return OpenAPI Service Address
     */
    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty(PropertyConstant.PROPERTY_SERVER_URL, cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getServerToken() {
        if (null == iastServerToken) {
            iastServerToken = System.getProperty(PropertyConstant.PROPERTY_SERVER_TOKEN,
                    cfg.getProperty("iast.server.token"));
        }
        return iastServerToken;
    }

    @Override
    public String toString() {
        return "[IastName=" + getIastName() +
                "，IastServerUrl=" + getBaseUrl() +
                "，IastServerToken=" + getServerToken() +
                "]";
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
            iastDumpPath = System.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_PATH,
                    cfg.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_PATH));
        }
        return iastDumpPath;
    }

    private String getDumpClassState() {
        if (null == dumpClassState) {
            dumpClassState = System.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_ENABLE,
                    cfg.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_ENABLE));
        }
        return dumpClassState;
    }

    public boolean isEnableDumpClass() {
        return "true".equalsIgnoreCase(getDumpClassState());
    }

    public long getHeartBeatInterval() {
        if (heartBeatInterval == -1L) {
            heartBeatInterval = Long.valueOf(System.getProperty(PropertyConstant.PROPERTY_SERVICE_HEARTBEAT_INTERVAL,
                    cfg.getProperty(PropertyConstant.PROPERTY_SERVICE_HEARTBEAT_INTERVAL, "60")));
        }
        return heartBeatInterval;
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
            proxyPort = Integer
                    .parseInt(System.getProperty(PropertyConstant.PROPERTY_PROXY_PORT,
                            cfg.getProperty(PropertyConstant.PROPERTY_PROXY_PORT, "80")));
        }
        return proxyPort;
    }

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty(PropertyConstant.PROPERTY_DEBUG, "false");
        }
        return debugFlag;
    }

    public Integer getResponseLength() {
        if (responseLength == null) {
            responseLength = Integer.parseInt(System.getProperty(PropertyConstant.PROPERTY_RESPONSE_LENGTH,
                    cfg.getProperty(PropertyConstant.PROPERTY_RESPONSE_LENGTH, "-1")));
        }
        return responseLength;
    }

    public String getPolicyPath() {
        if (null == this.policyPath) {
            this.policyPath = System.getProperty(PropertyConstant.PROPERTY_POLICY_PATH,
                    cfg.getProperty(PropertyConstant.PROPERTY_POLICY_PATH, ""));
        }
        return this.policyPath;
    }

    public static List<String> getDisabledPlugins() {
        return Optional.ofNullable(System.getProperty("dongtai.disabled.plugins"))
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(null);
    }

    public static List<String> getDisabledFeatures() {
        if (null == disabledFeatureList){
            disabledFeatureList = Optional.ofNullable(System.getProperty("dongtai.disabled.features"))
                    .map(s -> Arrays.asList(s.split(",")))
                    .orElse(new ArrayList<>());
        }
        return disabledFeatureList;
    }

    public static Boolean isDisabledCustomModel() {
        if (null == isDisabledCustomModel){
            List<String> disabledFeatures = getDisabledFeatures();
            isDisabledCustomModel = disabledFeatures.contains("custom-model-collection");
        }
        return isDisabledCustomModel;
    }
}
