package com.secnium.iast.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;

import org.junit.Test;

public class AgentTest {

    @Test
    public void appendToolsPath() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String pid = runtimeMXBean.getName().split("@")[0];
        try {
            pid = "94008";
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
