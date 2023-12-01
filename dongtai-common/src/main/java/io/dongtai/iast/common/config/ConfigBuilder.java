package io.dongtai.iast.common.config;

import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;
import org.json.*;

import java.util.HashMap;
import java.util.Map;

public class ConfigBuilder {
    private static ConfigBuilder instance;
    private final Map<ConfigKey, Config<?>> configMap = new HashMap<ConfigKey, Config<?>>();

    private ConfigBuilder() {
        this.configMap.put(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE,
                Config.<Integer>create(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE).setDefaultValue(5000));
        this.configMap.put(ConfigKey.REPORT_RESPONSE_BODY,
                Config.<Boolean>create(ConfigKey.REPORT_RESPONSE_BODY).setDefaultValue(true));
        this.configMap.put(ConfigKey.REQUEST_DENY_LIST,
                Config.<RequestDenyList>create(ConfigKey.REQUEST_DENY_LIST));
        this.configMap.put(ConfigKey.ENABLE_VERSION_HEADER,
                Config.<Boolean>create(ConfigKey.VERSION_HEADER_KEY).setDefaultValue(true));
        this.configMap.put(ConfigKey.VERSION_HEADER_KEY,
                Config.<String>create(ConfigKey.VERSION_HEADER_KEY).setDefaultValue("DongTai"));
        this.configMap.put(ConfigKey.ENABLE_LOGGER,
                Config.<Boolean>create(ConfigKey.ENABLE_LOGGER));
        this.configMap.put(ConfigKey.LOGGER_LEVEL,
                Config.<String>create(ConfigKey.LOGGER_LEVEL));
        this.configMap.put(ConfigKey.VALIDATED_SINK,
                Config.<Boolean>create(ConfigKey.VALIDATED_SINK).setDefaultValue(false));
    }

    public static ConfigBuilder getInstance() {
        if (instance == null) {
            instance = new ConfigBuilder();
        }
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    public Config<?> getConfig(ConfigKey key) {
        return this.configMap.get(key);
    }

    public void updateFromRemote(String content) {
        JSONObject config = null;
        try {
            JSONObject json = new JSONObject(content);
            config = json.getJSONObject("data");
        } catch (JSONException ignore) {
            DongTaiLog.error(ErrorCode.UTIL_CONFIG_LOAD_FAILED,ignore.getMessage());
        }

        update(config);
    }

    public void update(JSONObject config) {
        if (config == null) {
            return;
        }
        updateBool(config, ConfigKey.JsonKey.JSON_REPORT_RESPONSE_BODY);
        updateInt(config, ConfigKey.JsonKey.JSON_REPORT_MAX_METHOD_POOL_SIZE);
        updateBool(config, ConfigKey.JsonKey.JSON_ENABLE_VERSION_HEADER);
        updateString(config, ConfigKey.JsonKey.JSON_VERSION_HEADER_KEY);
        updateBool(config, ConfigKey.JsonKey.JSON_ENABLE_LOGGER);
        updateString(config, ConfigKey.JsonKey.JSON_LOGGER_LEVEL);
        updateBool(config, ConfigKey.JsonKey.JSON_VALIDATED_SINK);
        updateRequestDenyList(config);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey key) {
        try {
            return ((Config<T>) getConfig(key)).get();
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.UTIL_CONFIG_LOAD_FAILED,e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void updateBool(JSONObject config, ConfigKey.JsonKey jsonKey) {
        try {
            Config<Boolean> conf = (Config<Boolean>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                Boolean value = config.getBoolean(jsonKey.getKey());
                conf.setValue(value);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.UTIL_CONFIG_LOAD_FAILED,e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateInt(JSONObject config, ConfigKey.JsonKey jsonKey) {
        try {
            Config<Integer> conf = (Config<Integer>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                Integer value = config.getInt(jsonKey.getKey());
                conf.setValue(value);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.UTIL_CONFIG_LOAD_FAILED,e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateString(JSONObject config, ConfigKey.JsonKey jsonKey) {
        try {
            Config<String> conf = (Config<String>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                String value = config.getString(jsonKey.getKey());
                if (value != null && !value.isEmpty()) {
                    conf.setValue(value);
                }
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.UTIL_CONFIG_LOAD_FAILED,e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateRequestDenyList(JSONObject config) {
        try {
            ConfigKey.JsonKey jsonKey = ConfigKey.JsonKey.JSON_REQUEST_DENY_LIST;
            Config<RequestDenyList> conf = (Config<RequestDenyList>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                JSONArray value = config.getJSONArray(jsonKey.getKey());
                RequestDenyList requestDenyList = RequestDenyList.parse(value);
                conf.setValue(requestDenyList);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.UTIL_CONFIG_LOAD_FAILED,e.getMessage());
        }
    }
}
