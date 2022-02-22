package io.dongtai.iast.agent.middlewarerecognition.dubbo;

import io.dongtai.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class DubboService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass("org.apache.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (Exception ignored) {
        }
        try {
            loader.loadClass("com.alibaba.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (Exception ignored) {
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
