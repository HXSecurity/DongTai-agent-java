package io.dongtai.iast.agent.middlewarerecognition.spring;


import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class Tomcat implements IServer {

    private static final String TOMCAT_BOOTSTAP = " org.apache.catalina.startup.Bootstrap".substring(1);
    private static final String TOMCAT_SERVER_INFO = " org.apache.catalina.util.ServerInfo".substring(1);
    private String name;
    private String version;

    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            Class<?> serverInfo = loader.loadClass(TOMCAT_SERVER_INFO);
            name = (String) serverInfo.getMethod("getServerInfo").invoke(null);
            version = (String) serverInfo.getMethod("getServerNumber").invoke(null);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
