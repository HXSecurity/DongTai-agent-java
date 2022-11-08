package io.dongtai.iast.common.config;

public enum ConfigKey {
    REPORT_RESPONSE_BODY,
    REPORT_METHOD_POOL_MAX_SIZE,
    REQUEST_DENY_LIST,
    ;

    public enum JsonKey {
        JSON_REPORT_RESPONSE_BODY("gather_res_body", REPORT_RESPONSE_BODY),
        JSON_REPORT_METHOD_POOL_MAX_SIZE("method_pool_max_length", REPORT_METHOD_POOL_MAX_SIZE),
        JSON_REQUEST_DENY_LIST("blacklist_rules", REQUEST_DENY_LIST),
        ;

        private final String key;
        private final ConfigKey configKey;

        JsonKey(String key, ConfigKey configKey) {
            this.key = key;
            this.configKey = configKey;
        }

        public String getKey() {
            return this.key;
        }

        public ConfigKey getConfigKey() {
            return this.configKey;
        }
    }
}
