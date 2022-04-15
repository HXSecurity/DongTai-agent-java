package io.dongtai.iast.agent.middlewarerecognition.dubbo;

import io.dongtai.iast.agent.middlewarerecognition.IServer;
import io.dongtai.log.DongTaiLog;

import java.lang.management.RuntimeMXBean;

public class DubboService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean, ClassLoader loader) {
        try {
            loader.loadClass("org.apache.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
        try {
            loader.loadClass("com.alibaba.dubbo.monitor.support.MonitorFilter");
            return true;
        } catch (Exception e) {
            DongTaiLog.error(e);
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
