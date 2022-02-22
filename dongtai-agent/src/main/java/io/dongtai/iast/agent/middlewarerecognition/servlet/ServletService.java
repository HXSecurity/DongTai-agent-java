package io.dongtai.iast.agent.middlewarerecognition.servlet;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class ServletService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass("javax.servlet.ServletRequest");
            return true;
        } catch (Exception ignored) {
        }
        try {
            loader.loadClass("jakarta.servlet.ServletRequest");
            return true;
        } catch (Exception ignored) {
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
