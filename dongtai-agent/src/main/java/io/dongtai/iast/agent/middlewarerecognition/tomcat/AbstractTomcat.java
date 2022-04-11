package io.dongtai.iast.agent.middlewarerecognition.tomcat;


import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.iast.agent.middlewarerecognition.Supplier;

import java.lang.management.RuntimeMXBean;

abstract class AbstractTomcat implements IServer {
    private static final Supplier<TomcatVersion> TOMCAT_VERSION = getTomcatVersion();

    private static final String BOOTSTRAP = "bootstrap.jar";
    private static final String COMMAND = " org.apache.catalina.startup.Bootstrap start".substring(1);

    protected static boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader, TomcatVersion paramw) {
        return isBootstrap(paramRuntimeMXBean, loader) && isMatchVersion(paramw);
    }

    private static boolean isBootstrap(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        String classPath = paramRuntimeMXBean.getClassPath();
        String startCmd = paramRuntimeMXBean.getSystemProperties().get("sun.java.command");
        return classPath.contains(BOOTSTRAP) && startCmd.equals(COMMAND);
    }


    private static boolean isMatchVersion(TomcatVersion tomcat) {
        return (tomcat == TOMCAT_VERSION.get());
    }


    private static Supplier<TomcatVersion> getTomcatVersion() {
        return new Supplier<TomcatVersion>() {
            @Override
            public TomcatVersion get() {
                return TomcatRecognition.recognize();
            }
        };
    }
}