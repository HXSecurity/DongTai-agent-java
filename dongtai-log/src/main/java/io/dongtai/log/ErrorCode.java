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

    // agent engine init
    ENGINE_INSTALL_FAILED(20101, "engine install failed"),
    ENGINE_DESTROY_FAILED(20102, "engine destroy failed"),
    POLICY_LOAD_FAILED(20111, "load policy failed"),
    POLICY_CONFIG_INVALID(20112, "policy config invalid"),
    TRANSFORM_ENGINE_START_FAILED(20121, "transform engine start failed"),
    TRANSFORM_ENGINE_DESTROY_REDEFINE_CLASSES_FAILED(20122, "transform engine failed to redefine classes when destroy"),
    ENGINE_PROPERTIES_INITIALIZE_FAILED(20131, "engine properties initialize failed"),
    CLASS_DIAGRAM_SCAN_JAR_ANCESTOR_FAILED(20141, "class diagram scan jar ancestor failed"),

    // transform
    TRANSFORM_CLASS_FAILED(20201, "transform class {} failed"),
    TRANSFORM_CREATE_CLASS_DUMP_DIR_FAILED(20202, "transform create class dump dir {} failed"),
    TRANSFORM_CLASS_DUMP_FAILED(20203, "transform class dump failed"),
    RETRANSFORM_CLASS_CIRCULARITY_ERROR(20211, "retransform class {} ClassCircularityError: {}"),
    RETRANSFORM_CLASS_FAILED(20212, "retransform class {} failed"),
    ASM_CREATE_CLASS_STRUCTURE_FAILED(20221, "create class structure failed by using ASM, loader: {}"),
    ASM_CREATE_CLASS_STRUCTURE_BY_NAME_FAILED(20222, "create class failed failed by using ASM, class: {}, loader: {}"),

    // handler hookpoint
    SPY_LEAVE_HTTP_FAILED(20301, "hookpoint leave http failed"),
    SPY_COLLECT_HTTP_FAILED(20302, "hookpoint collect http {} failed"),
    SPY_COLLECT_METHOD_FAILED(20303, "hookpoint collect method failed"),
    SPY_TRACE_FEIGN_INVOKE_FAILED(20304, "hookpoint trace feign invoke failed"),
    SPY_METHOD_POOL_OVER_CAPACITY(20305, "current request method pool size over capacity: {}"),
    SPY_TRACE_DUBBO_INVOKE_FAILED(20306, "hookpoint trace dubbo invoke failed"),
    API_COLLECTOR_GET_API_THREAD_EXECUTE_FAILED(20311, "get api thread execute failed"),
    GRAPH_BUILD_AND_REPORT_FAILED(20321, "build and report request graph failed"),
    TAINT_COMMAND_GET_PARAMETERS_FAILED(20351, "taint command get {} parameters failed"),
    TAINT_COMMAND_RANGE_PROCESS_FAILED(20352, "taint command range process failed"),

    // report & replay
    REPORT_SEND_FAILED(20401, "send report to {} error, report: {}"),
    REPLAY_REQUEST_FAILED(20411, "replay request {} failed"),

    // SCA
    SCA_SCAN_JAR_LIB_FAILED(20501, "sca scan jar lib failed: {}, {}"),
    SCA_REPORT_SEND_FAILED(20502, "send sca report failed: {}, {}"),
    SCA_CALCULATE_JAR_SIGNATURE_FAILED(20503, "sca calculate jar signature failed: {}, {}"),

    // util
    UTIL_CONFIG_LOAD_FAILED(20601, "load config {} failed"),
    UTIL_TAINT_ADD_OBJECT_TO_POOL_FAILED(20611, "add object to taint pool failed"),
    UTIL_TAINT_PARSE_CUSTOM_MODEL_FAILED(20612, "parse custom model {} getter {} failed"),
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
