package io.dongtai.iast.agent.report;

import io.dongtai.iast.agent.Constant;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.iast.agent.middlewarerecognition.ServerDetect;
import io.dongtai.iast.agent.util.base64.Base64Encoder;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

import java.net.*;
import java.util.Enumeration;

import org.json.JSONObject;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentRegisterReport {

    private static AgentRegisterReport INSTANCE;
    private String projectName = null;
    private Integer agentId = -1;
    private static Integer coreRegisterStart = 1;
    final IServer server = ServerDetect.getWebserver();
    private static String AGENT_NAME = null;
    private static String HOST_NAME = null;

    /**
     * 创建agent注册报告
     *
     * @return agent注册报告
     */
    private String generateAgentRegisterMsg() {
        JSONObject object = new JSONObject();
        object.put(Constant.KEY_AGENT_TOKEN, AgentRegisterReport.getAgentToken());
        object.put(Constant.KEY_AGENT_VERSION, Constant.AGENT_VERSION_VALUE);
        object.put(Constant.KEY_PROJECT_NAME, getProjectName());
        object.put(Constant.KEY_CLUSTER_NAME, getClusterName());
        object.put(Constant.KEY_CLUSTER_VERSION, getClusterVersion());
        object.put(Constant.KEY_PID, EngineManager.getPID());
        object.put(Constant.KEY_HOSTNAME, AgentRegisterReport.getInternalHostName());
        object.put(Constant.KEY_LANGUAGE, Constant.LANGUAGE);
        object.put(Constant.KEY_NETWORK, readIpInfo());
        object.put(Constant.KEY_SERVER_ENV,
                Base64Encoder.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        object.put(Constant.KEY_CONTAINER_NAME, null == server ? "" : server.getName());
        object.put(Constant.KEY_CONTAINER_VERSION, null == server ? "" : server.getVersion());
        object.put(Constant.KEY_SERVER_PATH, ServerDetect.getWebServerPath());
        object.put(Constant.KEY_SERVER_ADDR, "");
        object.put(Constant.KEY_SERVER_PORT, "");
        object.put(Constant.KEY_AUTO_CREATE_PROJECT, IastProperties.getInstance().isAutoCreateProject());
        object.put(Constant.KEY_PROJECT_VERSION, IastProperties.getInstance().getProjectVersion());

        return object.toString();
    }

    /**
     * "火线~洞态IAST" 产品中现实的agent名称，可自行更改该代码实现自定义
     *
     * @return agent名称
     */
    public static String getAgentToken() {
        if (AGENT_NAME == null) {
            String osName = System.getProperty("os.name");
            String hostname = getInternalHostName();
            AGENT_NAME =
                    osName + "-" + hostname + "-" + Constant.AGENT_VERSION_VALUE + "-" + IastProperties.getInstance()
                            .getEngineName();
        }
        return AGENT_NAME;
    }

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    public static String getInternalHostName() {
        if (HOST_NAME == null) {
            if (System.getenv("COMPUTERNAME") != null) {
                HOST_NAME = System.getenv("COMPUTERNAME");
            } else {
                HOST_NAME = getHostNameForLinux();
            }
        }
        return HOST_NAME;
    }

    /**
     * 获取linux主机名
     *
     * @return 主机名
     */
    private static String getHostNameForLinux() {
        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    public static AgentRegisterReport getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new AgentRegisterReport();
        }
        return INSTANCE;
    }

    /**
     * 判断agent是否注册成功
     *
     * @return true - 注册成功；false - 注册失败
     */
    public boolean isRegistered() {
        return agentId != -1;
    }

    /**
     * 获取agent标识
     *
     * @return agent唯一标识，当前使用agentId
     */
    public static Integer getAgentFlag() {
        AgentRegisterReport registerReport = AgentRegisterReport.getInstance();
        return registerReport.agentId;
    }

    /**
     * 获取项目名称，用于自动绑定
     *
     * @return
     */
    private String getProjectName() {
        if (projectName == null) {
            IastProperties cfg = IastProperties.getInstance();
            projectName = cfg.getProjectName();
        }
        return projectName;
    }

    /**
     * 获取集群名称,用于集群对应多个分布式实例的应用agent管理
     *
     * @return {@link String}
     */
    private String getClusterName() {
        IastProperties cfg = IastProperties.getInstance();
        return cfg.getClusterName();
    }

    /**
     * 获取集群版本,用于集群对应多个分布式实例的应用agent管理
     *
     * @return {@link String}
     */
    private String getClusterVersion() {
        IastProperties cfg = IastProperties.getInstance();
        return cfg.getClusterVersion();
    }

    /**
     * 读取网卡信息
     *
     * @return
     */
    private String readIpInfo() {
        try {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<?> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) addresses.nextElement();
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }
                    if (first) {
                        sb.append("{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName())
                                .append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress())
                                .append("\"}");
                        first = false;
                    } else {
                        sb.append(",{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName())
                                .append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress())
                                .append("\"}");
                    }
                }
            }
            return sb.toString();
        } catch (SocketException e) {
            return "{}";
        }
    }

    public void register() {
        try {
            if (server == null) {
                DongTaiLog.warn("Can't Recognize Web Service");
                return;
            } else {
                DongTaiLog.info("DongTai will install for {} Service", server.getName());
            }
            String msg = generateAgentRegisterMsg();
            StringBuilder responseRaw = HttpClientUtils.sendPost(Constant.API_AGENT_REGISTER, msg);
            if (!isRegistered()) {
                setAgentData(responseRaw);
            }
        } catch (SocketTimeoutException e) {
            DongTaiLog.error("Agent registration to {} failed 10 seconds later, Reason: {}, token: {}",
                    IastProperties.getInstance().getBaseUrl(), IastProperties.getInstance().getIastServerToken());
        } catch (NullPointerException e) {
            DongTaiLog.error("Agent registration to {} failed, Token: {}, Reason: {}", IastProperties.getInstance().getBaseUrl(), IastProperties.getInstance().getIastServerToken(), e.getMessage());
        } catch (ConnectException e) {
            DongTaiLog.error("Agent registration failed, Reason: Failed to connect to {}, please check with `curl -v {}`", IastProperties.getInstance().getBaseUrl(), IastProperties.getInstance().getBaseUrl());
        } catch (Exception e) {
            DongTaiLog.error(e);
            DongTaiLog.error("Agent registration to {} failed 10 seconds later, cause: {}, token: {}",
                    IastProperties.getInstance().getBaseUrl(), e.getMessage(), IastProperties.getInstance().getIastServerToken());
        }
    }

    public static Boolean send() {
        AgentRegisterReport registerReport = AgentRegisterReport.getInstance();
        registerReport.register();
        return registerReport.isRegistered();
    }

    /**
     * 根据agent注册接口返回结果设置agent的id
     *
     * @param responseRaw
     */
    private void setAgentData(StringBuilder responseRaw) {
        JSONObject responseObj = new JSONObject(responseRaw.toString());
        Integer status = (Integer) responseObj.get("status");
        if (status == 201) {
            JSONObject data = (JSONObject) responseObj.get("data");
            agentId = (Integer) data.get("id");
            coreRegisterStart = (Integer) data.get("coreAutoStart");
        }
    }

    public static Boolean agentStat() {
        return coreRegisterStart == 1;
    }

}
