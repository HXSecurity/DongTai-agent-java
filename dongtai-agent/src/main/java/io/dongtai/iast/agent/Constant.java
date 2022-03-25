package io.dongtai.iast.agent;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Constant {
    public static final String AGENT_VERSION_VALUE = "v1.4.0";
    public static final String LANGUAGE = "JAVA";

    public final static String API_AGENT_REGISTER = "/api/v1/agent/register";
    public final static String API_REPORT_UPLOAD = "/api/v1/report/upload";
    public final static String API_ENGINE_ACTION = "/api/v1/engine/action";

    public static final String KEY_NETWORK = "network";
    public static final String KEY_AGENT_VERSION = "version";
    public static final String KEY_PID = "pid";
    public static final String KEY_AGENT_TOKEN = "name";
    public static final String KEY_CONTAINER_NAME = "containerName";
    public static final String KEY_CONTAINER_VERSION = "containerVersion";
    public static final String KEY_SERVER_PATH = "serverPath";
    public static final String KEY_SERVER_ADDR = "serverAddr";
    public static final String KEY_SERVER_PORT = "serverPort";
    public static final String KEY_HOSTNAME = "hostname";
    public static final String KEY_SERVER_ENV = "serverEnv";
    public static final String KEY_PROJECT_NAME = "projectName";
    public static final String KEY_CLUSTER_NAME = "clusterName";
    public static final String KEY_CLUSTER_VERSION = "clusterVersion";
    public static final String KEY_AUTO_CREATE_PROJECT = "autoCreateProject";
    public static final String KEY_PROJECT_VERSION = "projectVersion";
    public static final String KEY_LANGUAGE = "language";

    public static final int REPORT_HEART_BEAT = 0x01;
    public static final String KEY_UPDATE_REPORT = "type";
    public static final String KEY_REPORT_VALUE = "detail";
    public static final String KEY_AGENT_ID = "agentId";
    public static final String KEY_MEMORY = "memory";
    public static final String KEY_CPU = "cpu";
    public static final String KEY_DISK = "disk";
    public static final String KEY_PERFORMANCE = "performance";
    public static final String KEY_CORE_INSTALLED = "isCoreInstalled";
    public static final String KEY_CORE_RUNNING = "isCoreRunning";
    public static final String KEY_RETURN_QUEUE = "returnQueue";

    // 线程异常上报
    public static final int REPORT_ERROR_THREAD = 0x45;
    public static final String KEY_NOT_EXIST_THREADS = "notExistThread";
    public static final String KEY_HIGH_CPU_THREADS = "highCpuThread";

    public static final String THREAD_PREFIX = "DongTai-IAST-";

}
