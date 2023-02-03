package io.dongtai.log;

public enum ErrorCode {
    // prepare & common
    LOG_INITIALIZE_FAILED(10101, "log initialize failed"),
    LOG_CONFIGURE_FAILED(10102, "log configure failed"),
    FLUENT_SET_EXECUTABLE_FAILED(10111, "fluent setExecutable failed, please check {} permission"),
    FLUENT_EXTRACT_FAILED(10112, "fluent extract failed"),
    FLUENT_PROCESS_START_FAILED(10113, "fluent process start failed"),
    AGENT_PROPERTIES_INITIALIZE_FAILED(10121, "agent properties initialize failed"),
    AGENT_PROCESS_REMOTE_FALLBACK_CONFIG_FAILED(10122, "agent process remote fallback config failed, key: {}, valueType: {}"),
    AGENT_GET_RESOURCE_TO_FILE_FAILED(10131, "get resource {} to file {} failed: {}"),
    HTTP_CLIENT_PREPARE_REQUEST_BODY_FAILED(10141, "http client prepare request {} body failed"),
    HTTP_CLIENT_REQUEST_RESPONSE_CODE_INVALID(10142, "http client request {} response status code invalid: {}"),
    HTTP_CLIENT_REQUEST_PARSE_RESPONSE_FAILED(10143, "http client request {} parse response failed"),
    HTTP_CLIENT_REQUEST_EXECUTE_FAILED(10144, "http client request {} execute failed"),
    HTTP_CLIENT_REMOTE_FILE_RESPONSE_EMPTY(10145, "http client remote file {} response empty"),
    HTTP_CLIENT_REMOTE_FILE_RESPONSE_INVALID(10146, "http client remote file {} response invalid, code: {}, body: {}"),
    HTTP_CLIENT_REMOTE_FILE_DOWNLOAD_FAILED(10147, "http client remote file {} download failed"),

    // startup & register
    AGENT_PREMAIN_INVOKE_FAILED(10201, "agent premain invoke failed"),
    JATTACH_EXTRACT_FAILED(10202, "jattach extract failed. please check file {} permission"),
    JATTACH_EXECUTE_FAILED(10203, "jattach execute failure, please try again with command: {}"),
    AGENT_ATTACH_PARSE_ARGS_FAILED(10204, "agent attach parse args failed"),
    AGENT_ATTACH_INSTALL_FAILED(10205, "agent attach install failed"),
    AGENT_ATTACH_UNINSTALL_FAILED(10206, "agent attach uninstall failed"),
    AGENT_CANNOT_RECOGNIZE_WEB_SERVICE(10211, "agent can't recognize web service"),
    AGENT_REGISTER_REQUEST_FAILED(10212, "agent register to {} failed"),
    AGENT_REGISTER_RESPONSE_CODE_INVALID(10213, "agent register response code invalid: {}"),
    AGENT_REGISTER_PARSE_RESPONSE_FAILED(10214, "agent parse {} register response failed"),
    AGENT_REGISTER_INFO_INVALID(10215, "agent register info invalid, start without DongTai IAST"),

    // core trigger
    AGENT_EXTRACT_PACKAGES_FAILED(10301, "extract packages from agent failed"),
    AGENT_REFLECTION_INSTALL_FAILED(10302, "agent reflection install failed"),
    AGENT_REFLECTION_START_FAILED(10303, "agent reflection start failed"),
    AGENT_REFLECTION_STOP_FAILED(10304, "agent reflection stop failed"),
    AGENT_REFLECTION_UNINSTALL_FAILED(10305, "agent reflection uninstall failed"),

    // agent thread & monitor
    AGENT_GET_DONGTAI_THREAD_FAILED(10401, "agent get DongTai thread failed"),
    AGENT_KILL_DONGTAI_CORE_THREAD_FAILED(10402, "agent kill DongTai core thread failed"),
    AGENT_MONITOR_THREAD_CHECK_FAILED(10403, "agent monitor thread {} checked failed"),
    AGENT_MONITOR_COLLECT_PERFORMANCE_METRICS_FAILED(10411, "agent monitor collect {} performance metrics failed: {}"),
    AGENT_MONITOR_CHECK_PERFORMANCE_METRICS_FAILED(10412, "agent monitor check performance metrics failed: {}, {}"),
    AGENT_MONITOR_GET_DISK_USAGE_FAILED(10413, "agent monitor get disk usage failed: {}, {}"),

    // fallback
    AGENT_FALLBACK_SYNC_REMOTE_CONFIG_FAILED(10501, "agent fallback sync remote config failed"),
    AGENT_FALLBACK_METRICS_CONFIG_INVALID(10502, "agent fallback metrics config {} invalid: {}"),
    AGENT_FALLBACK_STATE_CHANGE_WITH_EXCEPTION(10503, "agent fallback state change to {}, but agent currently has exception"),
    AGENT_FALLBACK_CHECKER_CREATE_FAILED(10511, "agent fallback checker create failed: {}"),
    AGENT_FALLBACK_BREAKER_CONVERT_METRICS_FAILED(10521, "agent fallback breaker convert metrics failed: {}"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
