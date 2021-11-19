package com.secnium.iast.agent.middlewarerecognition;

import com.secnium.iast.agent.middlewarerecognition.jboss.JBoss;
import com.secnium.iast.agent.middlewarerecognition.jboss.JBossAS;
import com.secnium.iast.agent.middlewarerecognition.jetty.Jetty;
import com.secnium.iast.agent.middlewarerecognition.spring.Tomcat;
import com.secnium.iast.agent.middlewarerecognition.tomcat.*;
import com.secnium.iast.agent.middlewarerecognition.weblogic.WebLogic;
import com.secnium.iast.agent.middlewarerecognition.websphere.WebSphere;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServerDetect {
    /**
     * Web应用所在目录
     */
    private final String WebServerPath;
    /**
     * WebServer
     */
    private final IServer webserver;

    public static ServerDetect INSTANCE;

    private ServerDetect() {
        this.webserver = recognizeWebServer();
        this.WebServerPath = recognizeWebServerPath();
    }

    public static ServerDetect getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new ServerDetect();
        }
        return INSTANCE;
    }


    static IServer recognizeWebServer() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        for (IServer server : SERVERS) {
            if (server.isMatch(runtimeMXBean)) {
                return server;
            }
        }
        return null;
    }

    static String recognizeWebServerPath() {
        File file = new File(".");
        String path = file.getAbsolutePath();
        return path.substring(0, path.length() - 2);
    }


    public IServer getWebserver() {
        return this.webserver;
    }


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
    };


    public String getWebServerPath() {
        return WebServerPath;
    }

    public String getServeName() {
        if (null == webserver) {
            return "";
        }
        return webserver.getName();
    }
}