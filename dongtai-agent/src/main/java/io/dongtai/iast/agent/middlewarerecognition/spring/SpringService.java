package io.dongtai.iast.agent.middlewarerecognition.spring;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class SpringService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass("org.springframework.web.context.ConfigurableWebApplicationContext");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "Spring";
    }

    @Override
    public String getVersion() {
        return "";
    }
}
