package com.secnium.iast.core.report;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.vulscan.ReportConstant;
import com.secnium.iast.core.middlewarerecognition.IServer;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.util.Constants;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.base64.Base64Encoder;
import org.json.JSONObject;

import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.Enumeration;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentRegisterReport {
    private static String AGENT_NAME = null;
    private static String PROJECT_NAME = null;
    private static Integer AGENT_ID = -1;
    final static String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    final static ServerDetect SERVER_DETECT = ServerDetect.getInstance();
    final static IServer SERVER = SERVER_DETECT.getWebserver();

    private static String getAgentToken() {
        if (AGENT_NAME == null) {
            PropertyUtils cfg = PropertyUtils.getInstance();
            String osName = System.getProperty("os.name");
            String hostname = getInternalHostName();
            AGENT_NAME = osName + "-" + hostname + "-" + ReportConstant.AGENT_VERSION_VALUE + "-" + cfg.getEngineName();
        }
        return AGENT_NAME;
    }

    /**
     * 判断agent是否注册成功
     *
     * @return true - 注册成功；false - 注册失败
     */
    public static boolean isRegistered() {
        return AGENT_ID != -1;
    }

    /**
     * 获取agent标识
     *
     * @return agent唯一标识，当前使用agentId
     */
    public static Integer getAgentFlag() {
        return AGENT_ID;
    }

    /**
     * 获取项目名称，用于自动绑定
     *
     * @return
     */
    private static String getProjectName() {
        if (PROJECT_NAME == null) {
            PropertyUtils cfg = PropertyUtils.getInstance();
            PROJECT_NAME = cfg.getProjectName();
        }
        return PROJECT_NAME;
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

    /**
     * 获取主机名
     *
     * @return 主机名
     */
    private static String getInternalHostName() {
        if (System.getenv("COMPUTERNAME") != null) {
            return System.getenv("COMPUTERNAME");
        } else {
            return getHostNameForLinux();
        }
    }

    /**
     * 读取网卡信息
     *
     * @return
     */
    private static String readIpInfo() {
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
                        sb.append("{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName()).append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress()).append("\"}");
                        first = false;
                    } else {
                        sb.append(",{\"name\"").append(":").append("\"").append(networkInterface.getDisplayName()).append("\"");
                        sb.append(",\"ip\"").append(":").append("\"").append(inetAddress.getHostAddress()).append("\"}");
                    }
                }
            }
            return sb.toString();
        } catch (SocketException e) {
            return "{}";
        }
    }


    public static void send() {
        try {
            String msg = generateAgentRegisterMsg();
            StringBuilder responseRaw = HttpClientUtils.sendPost(Constants.API_AGENT_REGISTER, msg);
            if (!isRegistered()) {
                setAgentId(responseRaw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据agent注册接口返回结果设置agent的id
     *
     * @param responseRaw
     */
    private static void setAgentId(StringBuilder responseRaw) {
        JSONObject responseObj = new JSONObject(responseRaw.toString());
        Integer status = (Integer) responseObj.get("status");
        if (status == 201) {
            JSONObject data = (JSONObject) responseObj.get("data");
            AGENT_ID = (Integer) data.get("id");
        }
    }


    /**
     * 创建agent注册报告
     *
     * @return agent注册报告
     */
    private static String generateAgentRegisterMsg() {
        JSONObject object = new JSONObject();
        object.put("name", getAgentToken());
        object.put(ReportConstant.AGENT_VERSION, ReportConstant.AGENT_VERSION_VALUE);
        object.put(ReportConstant.PROJECT_NAME, getProjectName());
        object.put(ReportConstant.PID, PID);
        object.put(ReportConstant.HOSTNAME, getInternalHostName());
        object.put(ReportConstant.LANGUAGE, ReportConstant.LANGUAGE_VALUE);
        object.put(ReportConstant.NETWORK, readIpInfo());
        object.put(ReportConstant.SERVER_ENV, Base64Encoder.encodeBase64String(System.getProperties().toString().getBytes()).replaceAll("\n", ""));
        object.put(ReportConstant.CONTAINER_NAME, SERVER == null ? "" : SERVER.getName());
        object.put(ReportConstant.CONTAINER_VERSION, SERVER == null ? "" : SERVER.getVersion());
        object.put(ReportConstant.WEB_SERVER_PATH, SERVER_DETECT.getWebServerPath());
        object.put(ReportConstant.WEB_SERVER_ADDR, null != EngineManager.SERVER ? EngineManager.SERVER.getServerAddr() : "");
        object.put(ReportConstant.WEB_SERVER_PORT, null != EngineManager.SERVER ? EngineManager.SERVER.getServerPort() : "");

        return object.toString();
    }
}
