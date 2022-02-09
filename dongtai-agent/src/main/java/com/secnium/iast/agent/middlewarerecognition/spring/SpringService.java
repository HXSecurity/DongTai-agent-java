package com.secnium.iast.agent.middlewarerecognition.spring;

import com.secnium.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class SpringService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        try {
            Class<?> classOfSpringContext = Thread.currentThread().getContextClassLoader().loadClass("org.springframework.web.context.ConfigurableWebApplicationContext");
            return true;
        } catch (Exception e) {

        }
        return false;
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
