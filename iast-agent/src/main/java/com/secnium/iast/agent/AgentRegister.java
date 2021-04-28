package com.secnium.iast.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentRegister {
    private static String AGENT_NAME = null;

    /**
     * "火线~洞态IAST" 产品中现实的agent名称，可自行更改该代码实现自定义
     *
     * @return agent名称
     */
    public static String getAgentToken() {
        if (AGENT_NAME == null) {
            String osName = System.getProperty("os.name");
            String hostname = getInternalHostName();
            AGENT_NAME = osName + "-" + hostname + "-" + Constant.AGENT_VERSION_VALUE + "-" + IastProperties.getInstance().getEngineName();
        }
        return AGENT_NAME;
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
}
