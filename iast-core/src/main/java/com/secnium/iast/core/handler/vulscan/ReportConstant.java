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
    public static final String AGENT_ID = "agentId";
    public static final String PROJECT_NAME = "projectName";
    public static final String AGENT_VERSION = "version";
    public static final String AGENT_REPORT_VERSION = "agentVersion";
    public static final String AGENT_VERSION_VALUE = "v1.0.6";
    public static final String CONTAINER = "container";
    public static final String APP_NAME = "appName";
    public static final String APP_PATH = "appPath";

    public static final String PID = "pid";
    public static final String NETWORK = "network";
    public static final String MEMORY = "memory";
    public static final String CPU = "cpu";
    public static final String DISK = "disk";
    public static final String REQ_COUNT = "reqCount";
    public static final String CONTAINER_NAME = "containerName";
    public static final String CONTAINER_VERSION = "containerVersion";
    public static final String SERVER_PATH = "serverPath";
    public static final String SERVER_ADDR = "serverAddr";
    public static final String SERVER_NAME = "serverName";
    public static final String SERVER_PORT = "serverPort";
    public static final String HOSTNAME = "hostname";

    public static final String SCA_PACKAGE_PATH = "packagePath";
    public static final String SCA_PACKAGE_NAME = "packageName";
    public static final String SCA_PACKAGE_SIGNATURE = "packageSignature";
    public static final String SCA_PACKAGE_ALGORITHM = "packageAlgorithm";

    public static final String COMMON_SERVER_NAME = "server_name";
    public static final String COMMON_SERVER_PORT = "server_port";
    public static final String PROTOCOL = "protocol";
    public static final String HTTP_PROTOCOL = "httpProtocol";
    public static final String SCHEME = "scheme";
    public static final String HTTP_SCHEME = "httpScheme";
    public static final String METHOD = "method";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String SECURE = "secure";
    public static final String HTTP_SECURE = "httpSecure";
    public static final String URL = "url";
    public static final String HTTP_URL = "httpUrl";
    public static final String URI = "uri";
    public static final String HTTP_URI = "httpUri";
    public static final String QUERY_STRING = "queryString";
    public static final String HTTP_QUERY_STRING = "httpQueryString";
    public static final String REQ_HEADER = "reqHeader";
    public static final String HTTP_REQ_HEADER = "httpReqHeader";
    public static final String REQ_BODY = "reqBody";
    public static final String HTTP_BODY = "httpBody";
    public static final String CLIENT_IP = "clientIp";
    public static final String HTTP_CLIENT_IP = "httpClientIp";
    public static final String CONTEXT_PATH = "contextPath";
    public static final String RES_HEADER = "resHeader";
    public static final String HTTP_RES_HEADER = "httpResHeader";
    public static final String RES_BODY = "resBody";
    public static final String HTTP_RES_BODY = "httpResBody";
    public static final String REPLAY_REQUEST = "replayRequest";
    public static final String HTTP_REPLAY_REQUEST = "httpReplayRequest";
    public static final String SERVER_ENV = "serverEnv";
    public static final String VULN_CALLER = "appCaller";
    public static final String SAAS_METHOD_POOL = "pool";

    public static final String ERROR_LOG_DETAIL = "log";

    public static final String VULN_TYPE = "vulnType";

    public static final String API_DATA = "api_data";

    public static final String REPORT_QUEUE = "reportQueue";
    public static final String METHOD_QUEUE = "methodQueue";
    public static final String REPLAY_QUEUE = "replayQueue";

    public static final String AUTO_CREATE_PROJECT = "autoCreateProject";
    public static final String STARTUP_TIME = "startupTime";
}
