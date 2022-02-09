package com.secnium.iast.agent.middlewarerecognition.dubbo;

import com.secnium.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class DubboService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.apache.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Thread.currentThread().getContextClassLoader().loadClass("com.alibaba.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    @Override
    public String getName() {
        return "Dubbo";
    }

    @Override
    public String getVersion() {
        return null;
    }
}
