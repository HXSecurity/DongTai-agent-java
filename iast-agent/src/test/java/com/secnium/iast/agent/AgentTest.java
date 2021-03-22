package com.secnium.iast.agent;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class AgentTest {
    @Test
    public void appendToolsPath() {
        Agent.appendToolsPath();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String pid = runtimeMXBean.getName().split("@")[0];
        AttachLauncher.attach(pid, "");
    }
}
