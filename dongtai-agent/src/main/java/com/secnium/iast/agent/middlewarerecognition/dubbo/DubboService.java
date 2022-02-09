package com.secnium.iast.agent.middlewarerecognition.dubbo;

import com.secnium.iast.agent.middlewarerecognition.IServer;

import java.lang.management.RuntimeMXBean;

public class DubboService implements IServer {
    @Override
    public boolean isMatch(RuntimeMXBean paramRuntimeMXBean) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }
}
