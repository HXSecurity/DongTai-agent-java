package io.dongtai.iast.core.utils;

import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.config.ConfigKey;
import io.dongtai.iast.common.constants.PropertyConstant;
import io.dongtai.iast.common.exception.DongTaiIastException;
import io.dongtai.iast.common.string.StringUtils;
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
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private Integer responseLength;
    private String policyPath;
    private static List<String> disabledFeatureList;
    private static Boolean isDisabledCustomModel;

    private final String propertiesFilePath;

    public static final Integer DEFAULT_TAINT_TO_STRING_CHAR_LIMIT = 1024;
    public static final Integer DEFAULT_POOL_CAPACITY = 4096;
    public static final Integer DEFAULT_POOL_SIZE = 0;
    public static final Integer DEFAULT_POOL_MAX_SIZE = 10;
    public static final Integer DEFAULT_POOL_KEEPALIVE = 10;

    // 污点转换为字符串的时候字符数长度限制
    private Integer taintToStringCharLimit = DEFAULT_TAINT_TO_STRING_CHAR_LIMIT;
    private Integer poolCapacity;
    private Integer poolSize;
    private Integer poolMaxSize;
    private Integer poolKeepalive;

    public static PropertyUtils getInstance(String propertiesFilePath) throws DongTaiPropertyConfigException, DongTaiEnvConfigException {
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

    private PropertyUtils(String propertiesFilePath) throws DongTaiPropertyConfigException, DongTaiEnvConfigException {
        this.propertiesFilePath = propertiesFilePath;
        init();
    }

    /**
     * 根据配置文件初始化单例配置类
     */
    private void init() throws DongTaiPropertyConfigException, DongTaiEnvConfigException {
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

        // 初始化一些参数
        this.initTaintToStringCharLimit();
        this.initPool();
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
        return "io.dongtai.iast.resources/blacklist.txt";
    }

    public String getBlackUrl() {
        return "io.dongtai.iast.resources/blackurl.txt";
    }

    public String getBlackExtFilePath() {
        return "io.dongtai.iast.resources/blackext.txt";
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
        if (null == disabledFeatureList) {
            disabledFeatureList = Optional.ofNullable(System.getProperty("dongtai.disabled.features"))
                    .map(s -> Arrays.asList(s.split(",")))
                    .orElse(new ArrayList<>());
        }
        return disabledFeatureList;
    }

    public static Boolean isDisabledCustomModel() {
        if (null == isDisabledCustomModel) {
            List<String> disabledFeatures = getDisabledFeatures();
            isDisabledCustomModel = disabledFeatures.contains("custom-model-collection");
        }
        return isDisabledCustomModel;
    }

    public static Boolean validatedSink() {
        return ConfigBuilder.getInstance().get(ConfigKey.VALIDATED_SINK);
    }

    /**
     * 污点转为字符串时的长度限制
     *
     * @return
     */
    public static Integer getTaintToStringCharLimit() {
        if (instance == null) {
            return DEFAULT_TAINT_TO_STRING_CHAR_LIMIT;
        }
        return instance.taintToStringCharLimit;
    }

    public Integer getPoolCapacity() {
        if (instance == null) {
            return DEFAULT_POOL_CAPACITY;
        }
        return instance.poolCapacity;
    }

    public Integer getPoolSize() {
        if (instance == null) {
            return DEFAULT_POOL_SIZE;
        }
        return instance.poolSize;
    }

    public Integer getPoolMaxSize() {
        if (instance == null) {
            return DEFAULT_POOL_MAX_SIZE;
        }
        return instance.poolMaxSize;
    }

    public Integer getPoolKeepalive() {
        if (instance == null) {
            return DEFAULT_POOL_KEEPALIVE;
        }
        return instance.poolKeepalive;
    }

    /**
     * 初始化taintToStringCharLimit参数的值
     *
     * @throws DongTaiPropertyConfigException
     * @throws DongTaiEnvConfigException
     */
    public void initTaintToStringCharLimit() throws DongTaiPropertyConfigException, DongTaiEnvConfigException {

        // 1. 先从配置文件中读取
        String s = cfg.getProperty(PropertyConstant.PROPERTY_TAINT_LENGTH);
        if (!StringUtils.isBlank(s)) {
            this.taintToStringCharLimit = Integer.parseInt(s.trim());
            if (this.taintToStringCharLimit <= 0) {
                throw new DongTaiPropertyConfigException("The value of this parameter " + PropertyConstant.PROPERTY_TAINT_LENGTH
                        + " value " + s + " in your configuration file " + this.propertiesFilePath + " is illegal, such as passing an number greater than 1");
            }
        }

        // 2. 然后从环境变量中读取
        s = System.getProperty(PropertyConstant.PROPERTY_TAINT_LENGTH);
        if (!StringUtils.isBlank(s)) {
            this.taintToStringCharLimit = Integer.parseInt(s.trim());
            if (this.taintToStringCharLimit <= 0) {
                throw new DongTaiEnvConfigException("The value of this parameter " + PropertyConstant.PROPERTY_TAINT_LENGTH
                        + " value " + s + " in your environment variables is illegal, such as passing an number greater than 1");
            }
        }

    }

    private void initPool() throws DongTaiPropertyConfigException, DongTaiEnvConfigException {
        this.poolCapacity = parseAndSetProperty(PropertyConstant.PROPERTY_POOL_CAPACITY, DEFAULT_POOL_CAPACITY);
        this.poolSize = parseAndSetProperty(PropertyConstant.PROPERTY_POOL_SIZE, DEFAULT_POOL_SIZE);
        this.poolMaxSize = parseAndSetProperty(PropertyConstant.PROPERTY_POOL_MAX_SIZE, DEFAULT_POOL_MAX_SIZE);
        this.poolKeepalive = parseAndSetProperty(PropertyConstant.PROPERTY_POOL_KEEPALIVE, DEFAULT_POOL_KEEPALIVE);
    }

    private Integer parseAndSetProperty(String propertyKey,Integer defaultValue) throws DongTaiPropertyConfigException, DongTaiEnvConfigException {
        String propertyStr = cfg.getProperty(propertyKey);
        Integer value = defaultValue;
        if (!StringUtils.isBlank(propertyStr)) {
            value = Integer.parseInt(propertyStr.trim());
            if (value <= 0) {
                throw new DongTaiPropertyConfigException("The value of parameter " + propertyKey
                        + " value " + propertyStr + " in your configuration file " + this.propertiesFilePath + " is illegal, such as passing a number greater than 1");
            }
        }

        // 2. 然后从环境变量中读取
        propertyStr = System.getProperty(propertyKey);
        if (!StringUtils.isBlank(propertyStr)) {
            value = Integer.parseInt(propertyStr.trim());
            if (value <= 0) {
                throw new DongTaiEnvConfigException("The value of this parameter " + propertyKey
                        + " value " + propertyStr + " in your environment variables is illegal, such as passing an number greater than 1");
            }
        }
        return value;
    }

    /**
     * Property文件配置错误
     */
    public static class DongTaiPropertyConfigException extends DongTaiIastException {

        public DongTaiPropertyConfigException() {
        }

        public DongTaiPropertyConfigException(String message) {
            super(message);
        }

        public DongTaiPropertyConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        public DongTaiPropertyConfigException(Throwable cause) {
            super(cause);
        }

        public DongTaiPropertyConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * 环境变量传参配置错误
     */
    public static class DongTaiEnvConfigException extends DongTaiIastException {

        public DongTaiEnvConfigException() {
        }

        public DongTaiEnvConfigException(String message) {
            super(message);
        }

        public DongTaiEnvConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        public DongTaiEnvConfigException(Throwable cause) {
            super(cause);
        }

        public DongTaiEnvConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
