package io.dongtai.iast.agent.report;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.iast.agent.middlewarerecognition.ServerDetect;
import io.dongtai.iast.agent.util.http.HttpClientUtils;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.constants.ApiPath;
import io.dongtai.iast.common.utils.base64.Base64Encoder;
import io.dongtai.log.DongTaiLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.util.Enumeration;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentRegisterReport {

    public static AgentRegisterReport INSTANCE;
    private String projectName = null;
    private Integer agentId = -1;
    private static Integer coreRegisterStart = 1;
    final IServer server = ServerDetect.getWebserver();
    private static String AGENT_NAME = null;
    private static String HOST_NAME = null;

    public static void setINSTANCE(AgentRegisterReport INSTANCE) {
        AgentRegisterReport.INSTANCE = INSTANCE;
    }

    /**
     * 创建agent注册报告
     *
     * @return agent注册报告
     */
    private String generateAgentRegisterMsg() {
        JSONObject object = new JSONObject();
        object.put("name", AgentRegisterReport.getAgentToken());
        object.put("version", AgentConstant.VERSION_VALUE);
        object.put("projectName", getProjectName());
        object.put("clusterName", getClusterName());
        object.put("clusterVersion", getClusterVersion());
        object.put("pid", EngineManager.getPID());
        object.put("hostname", AgentRegisterReport.getInternalHostName());
        object.put("language", AgentConstant.LANGUAGE);
        object.put("network", readIpInfo());
        object.put("serverEnv",
                Base64Encoder.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        object.put("containerName", null == server ? "" : server.getName());
        object.put("containerVersion", null == server ? "" : server.getVersion());
        object.put("serverPath", ServerDetect.getWebServerPath());
        object.put("serverAddr", "");
        object.put("serverPort", "");
        object.put("autoCreateProject", IastProperties.getInstance().isAutoCreateProject());
        object.put("projectVersion", IastProperties.getInstance().getProjectVersion());

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
                    osName + "-" + hostname + "-" + AgentConstant.VERSION_VALUE + "-" + IastProperties.getInstance()
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
            Enumeration<?> interfaces = NetworkInterface.getNetworkInterfaces();
            JSONArray network = new JSONArray();
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
                    JSONObject jsonObject = new JSONObject();
                    String displayName = networkInterface.getDisplayName();
                    String hostAddress = inetAddress.getHostAddress();
                    jsonObject.put("name",displayName);
                    jsonObject.put("ip",hostAddress);
                    if (displayName.startsWith("en")){
                        jsonObject.put("isAddress","1");
                    }else {
                        jsonObject.put("isAddress","0");
                    }
                    network.put(jsonObject);
                }
            }
            return network.toString();
        } catch (SocketException e) {
            return "{}";
        }
    }

    public void register(){
        try {
            if (server == null) {
                DongTaiLog.warn("Can't Recognize Web Service");
                return;
            } else {
                DongTaiLog.debug("DongTai will install for {} Service", server.getName());
            }
            String msg = generateAgentRegisterMsg();
            StringBuilder responseRaw = HttpClientUtils.sendPost(ApiPath.AGENT_REGISTER, msg);
            if (!isRegistered()) {
                setAgentData(responseRaw);
            }
        } catch (NullPointerException e) {
            DongTaiLog.error("Agent registration to {} failed, Token: {}, Reason: {}",
                    IastProperties.getInstance().getBaseUrl(), IastProperties.getInstance().getServerToken(), e.getMessage());
        } catch (Exception e) {
            DongTaiLog.error("Agent registration to {} failed 10 seconds later, cause: {}, token: {}",
                    IastProperties.getInstance().getBaseUrl(), e.toString(), IastProperties.getInstance().getServerToken());
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
        try {
            JSONObject responseObj = new JSONObject(responseRaw.toString());
            Integer status = (Integer) responseObj.get("status");
            if (status == 201) {
                JSONObject data = (JSONObject) responseObj.get("data");
                agentId = (Integer) data.get("id");
                coreRegisterStart = (Integer) data.get("coreAutoStart");
            } else {
                DongTaiLog.error("Register msg: " + responseRaw);
            }
        } catch (Exception e) {
            DongTaiLog.error("Parse {} register response failed: {}",
                    IastProperties.getInstance().getBaseUrl(), e.toString());
        }
    }

    public static Boolean agentStat() {
        return coreRegisterStart == 1;
    }

}
