package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Properties;

public class HeartBeatReportTest {
    @Test
    public void testReadIpInfo() {
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = mxBean.getInputArguments();
        for (String argument : arguments) {
            System.out.println("argument = " + argument);
        }
    }

    @Test
    public void getProperties() {
        System.out.println(System.getProperties().toString());
    }

    @Test
    public void testGetCurrentPath() {
        String currentPath = HeartBeatReport.getCurrentPath();
        System.out.println("path：" + currentPath);
    }

    @Test
    public void getWebServerPath() {
        String serverPath = HeartBeatReport.getWebServerPath();
        System.out.println("hostname = " + serverPath);
    }

    @Test
    public void testGetHostName() {
        Properties properties = System.getProperties();
        HeartBeatReport heartBeatReport = HeartBeatReport.getInstance();
        String hostname = heartBeatReport.getHostName();
        System.out.println("hostname = " + hostname);
    }

    @Test
    public void testGetPid() {
        HeartBeatReport heartBeatReport = HeartBeatReport.getInstance();
        String pid = heartBeatReport.getPid();
        System.out.println("pid = " + pid);
    }

    @Test
    public void testSend() throws Exception {
        PropertyUtils.getInstance("～/.iast/config/iast.properties");
        AgentRegisterReport.send();

        HeartBeatReport report = new HeartBeatReport(1000);
        report.send();

        VulnReport report1 = new VulnReport(1000);
        report1.send();
    }
}
