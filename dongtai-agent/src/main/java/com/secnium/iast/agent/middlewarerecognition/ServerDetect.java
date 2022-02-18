package com.secnium.iast.agent.middlewarerecognition;

import com.secnium.iast.agent.Agent;
import com.secnium.iast.agent.AgentLauncher;
import com.secnium.iast.agent.middlewarerecognition.dubbo.DubboService;
import com.secnium.iast.agent.middlewarerecognition.gRPC.GrpcService;
import com.secnium.iast.agent.middlewarerecognition.jboss.JBoss;
import com.secnium.iast.agent.middlewarerecognition.jboss.JBossAS;
import com.secnium.iast.agent.middlewarerecognition.jetty.Jetty;
import com.secnium.iast.agent.middlewarerecognition.servlet.ServletService;
import com.secnium.iast.agent.middlewarerecognition.spring.SpringService;
import com.secnium.iast.agent.middlewarerecognition.spring.Tomcat;
import com.secnium.iast.agent.middlewarerecognition.tomcat.*;
import com.secnium.iast.agent.middlewarerecognition.weblogic.WebLogic;
import com.secnium.iast.agent.middlewarerecognition.websphere.WebSphere;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServerDetect {
    private static final IServer[] SERVERS = {
            new Tomcat(),
            new TomcatV9(),
            new TomcatV8(),
            new TomcatV7(),
            new TomcatV6(),
            new TomcatV5(),
            new Jetty(),
            new JBoss(),
            new JBossAS(),
            new WebSphere(),
            new WebLogic(),
            new SpringService(),
            new ServletService(),
            new DubboService(),
            new GrpcService(),
            new UnknownService()
    };

    public static IServer getWebserver() {
        if (AgentLauncher.LAUNCH_MODE.equals(AgentLauncher.LAUNCH_MODE_ATTACH)) {
            return new UnknownService();
        }
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (IServer server : SERVERS) {
            if (server.isMatch(runtimeMXBean, loader)) {
                return server;
            }
        }
        return null;
    }

    public static String getWebServerPath() {
        File file = new File(".");
        String path = file.getAbsolutePath();
        return path.substring(0, path.length() - 2);
    }
}