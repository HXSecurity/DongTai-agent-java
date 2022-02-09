package com.secnium.iast.agent.middlewarerecognition.servlet;

import com.secnium.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class ServletService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("javax.servlet.ServletRequest");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Thread.currentThread().getContextClassLoader().loadClass("jakarta.servlet.ServletRequest");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    @Override
    public String getName() {
        return "Servlet";
    }

    @Override
    public String getVersion() {
        return "";
    }
}
