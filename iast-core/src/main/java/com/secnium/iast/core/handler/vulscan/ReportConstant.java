package com.secnium.iast.core.handler.vulscan;

/**
 * 定义iast云端报告中需要使用的常量数据
 *
 * @author dongzhiyong@huoxian.cn
 */
public class ReportConstant {

    public static final String REPORT_KEY = "type";
    public static final int REPORT_HEART_BEAT = 0x01;
    public static final int REPORT_SCA = 0x11;
    public static final int REPORT_VULN_NORNAL = 0x21;
    public static final int REPORT_VULN_DYNAMIC = 0x22;
    public static final int REPORT_VULN_OVER_POWER = 0x23;
    public static final int REPORT_VULN_SAAS_POOL = 0x24;
    public static final int REPORT_AUTH_ADD = 0x31;
    public static final int REPORT_AUTH_UPDATE = 0x32;
    public static final int REPORT_ERROR_LOG = 0x51;
    public static final int REPORT_API = 0x61;


    public static final String REPORT_VALUE_KEY = "detail";

    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_VALUE = "JAVA";
    public static final String AGENT_ID = "agent_id";
    public static final String PROJECT_NAME = "project_name";
    public static final String AGENT_VERSION = "version";
    public static final String AGENT_VERSION_VALUE = "v1.0.5";
    public static final String CONTAINER = "container";
    public static final String CONTAINER_PATH = "container_path";

    public static final String PID = "pid";
    public static final String NETWORK = "network";
    public static final String HEART_BEAT_MEMORY = "memory";
    public static final String HEART_BEAT_CPU = "cpu";
    public static final String HEART_BEAT_DISK = "disk";
    public static final String HEART_BEAT_REQ_COUNT = "req_count";
    public static final String CONTAINER_NAME = "container_name";
    public static final String CONTAINER_VERSION = "container_version";
    public static final String WEB_SERVER_PATH = "web_server_path";
    public static final String WEB_SERVER_ADDR = "web_server_addr";
    public static final String WEB_SERVER_PORT = "web_server_port";
    public static final String HOSTNAME = "hostname";

    public static final String SCA_PACKAGE_PATH = "package_path";
    public static final String SCA_PACKAGE_NAME = "package_name";
    public static final String SCA_PACKAGE_SIGNATURE = "package_signature";
    public static final String SCA_PACKAGE_ALGORITHM = "package_algorithm";

    public static final String COMMON_APP_NAME = "app_name";
    public static final String COMMON_SERVER_NAME = "server_name";
    public static final String COMMON_SERVER_PORT = "server_port";
    public static final String COMMON_REMOTE_IP = "app_name";
    public static final String COMMON_HTTP_PROTOCOL = "http_protocol";
    public static final String COMMON_HTTP_SCHEME = "http_scheme";
    public static final String COMMON_HTTP_METHOD = "http_method";
    public static final String COMMON_HTTP_SECURE = "http_secure";
    public static final String COMMON_HTTP_URL = "http_url";
    public static final String COMMON_HTTP_URI = "http_uri";
    public static final String COMMON_HTTP_QUERY_STRING = "http_query_string";
    public static final String COMMON_HTTP_REQ_HEADER = "http_req_header";
    public static final String COMMON_HTTP_BODY = "http_body";
    public static final String COMMON_HTTP_CLIENT_IP = "http_client_ip";
    public static final String COMMON_HTTP_CONTEXT_PATH = "context_path";
    public static final String COMMON_HTTP_RES_HEADER = "http_res_header";
    public static final String COMMON_HTTP_RES_BODY = "http_res_body";
    public static final String COMMON_HTTP_REPLAY_REQUEST = "http_replay_request";
    public static final String SERVER_ENV = "server_env";
    public static final String VULN_CALLER = "app_caller";
    public static final String TAINT_VALUE = "taint_value";
    public static final String TAINT_POSITION = "taint_position";
    public static final String TAINT_PARAM_NAME = "param_name";
    public static final String SAAS_METHOD_POOL = "pool";

    public static final String AUTH_ADD_JDBC_CLASS = "jdbc_class";
    public static final String AUTH_ADD_VALUE = "auth_value";
    public static final String AUTH_ADD_SQL_STATEMENT = "auth_sql";
    public static final String AUTH_UPDATE_ORIGINA = "auth_original";
    public static final String AUTH_UPDATE_UPDATED = "auth_updated";

    public static final String ERROR_LOG_DETAIL = "log";

    public static final String VULN_TYPE = "vuln_type";

    public static final String OVER_POWER_SQL = "sql";
    public static final String OVER_POWER_AUTH_COOKIE = "cookie";
    public static final String OVER_POWER_TRACE_ID = "x-trace-id";

    public static final String API_DATA = "api_data";
    public static final String API_DATA_URI = "uri";
    public static final String API_DATA_METHOD = "method";
    public static final String API_DATA_CLASS = "class";
    public static final String API_DATA_PARAMETERS = "parameters";
    public static final String API_DATA_PARAMETER_NAME = "name";
    public static final String API_DATA_PARAMETER_TYPE = "type";
    public static final String API_DATA_PARAMETER_ANNOTATION = "annotation";
    public static final String API_DATA_RETURN = "return_type";
    public static final String API_DATA_FILE = "file";
    public static final String API_DATA_CONTROLLER = "controller";
    public static final String API_DATA_DESCRIPTION = "description";
  
    public static final String REPORT_QUEUE = "report_queue";
    public static final String METHOD_QUEUE = "method_queue";
    public static final String REPLAY_QUEUE = "replay_queue";
}
