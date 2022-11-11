package io.dongtai.iast.common.config;

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
                Config.<RequestDenyList>create(ConfigKey.REQUEST_DENY_LIST).setDefaultValue(null));
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
        }

        update(config);
    }

    public void update(JSONObject config) {
        if (config == null) {
            return;
        }
        updateBool(config, ConfigKey.JsonKey.JSON_REPORT_RESPONSE_BODY);
        updateInt(config, ConfigKey.JsonKey.JSON_REPORT_MAX_METHOD_POOL_SIZE);
        updateRequestDenyList(config);
    }

    @SuppressWarnings("unchecked")
    private void updateBool(JSONObject config, ConfigKey.JsonKey jsonKey) {
        try {
            Config<Boolean> conf = (Config<Boolean>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                Boolean value = config.getBoolean(jsonKey.getKey());
                conf.setValue(value);
            }
        } catch (Throwable ignore) {
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
        } catch (Throwable ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    private void updateString(JSONObject config, ConfigKey.JsonKey jsonKey) {
        try {
            Config<String> conf = (Config<String>) getConfig(jsonKey.getConfigKey());
            if (conf != null) {
                String value = config.getString(jsonKey.getKey());
                conf.setValue(value);
            }
        } catch (Throwable ignore) {
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
        } catch (Throwable ignore) {
        }
    }
}
