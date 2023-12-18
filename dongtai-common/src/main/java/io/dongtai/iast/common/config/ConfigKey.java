package io.dongtai.iast.common.config;

public enum ConfigKey {
    REPORT_RESPONSE_BODY,
    REPORT_MAX_METHOD_POOL_SIZE,
    REQUEST_DENY_LIST,
    ENABLE_VERSION_HEADER,
    VERSION_HEADER_KEY,
    ENABLE_LOGGER,
    LOGGER_LEVEL,
    VALIDATED_SINK,
    ENABLE_QPS_RATE_LIMIT,
    QPS_RATE_LIMIT,
    TOKEN_BUCKET_POOL_SIZE,
    ;

    public enum JsonKey {
        JSON_REPORT_RESPONSE_BODY("gather_res_body", REPORT_RESPONSE_BODY),
        JSON_REPORT_MAX_METHOD_POOL_SIZE("method_pool_max_length", REPORT_MAX_METHOD_POOL_SIZE),
        JSON_REQUEST_DENY_LIST("blacklist_rules", REQUEST_DENY_LIST),
        JSON_ENABLE_VERSION_HEADER("enable_version_header", ENABLE_VERSION_HEADER),
        JSON_VERSION_HEADER_KEY("version_header_name", VERSION_HEADER_KEY),
        JSON_ENABLE_LOGGER("enable_log", ENABLE_LOGGER),
        JSON_LOGGER_LEVEL("log_level", LOGGER_LEVEL),
        JSON_VALIDATED_SINK("report_validated_sink", VALIDATED_SINK),
        JSON_ENABLE_QPS_RATE_LIMIT("enable_qps_rate_limit", ENABLE_QPS_RATE_LIMIT),
        JSON_QPS_RATE_LIMIT("qps_rate_limit", QPS_RATE_LIMIT),
        JSON_TOKEN_BUCKET_POOL_SIZE("token_bucket_pool_size", TOKEN_BUCKET_POOL_SIZE),
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
