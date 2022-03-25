package com.secnium.iast.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;

import io.dongtai.log.DongTaiLog;
import org.junit.Test;

public class AgentTest {

    @Test
    public void appendToolsPath() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String pid = runtimeMXBean.getName().split("@")[0];
        try {
            pid = "94008";
        } catch (Throwable e) {
            DongTaiLog.error(e);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }
}
