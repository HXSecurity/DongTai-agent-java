package com.secnium.iast.core.report;

import com.secnium.iast.core.PropertyUtils;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

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
    public void testSend() throws Exception {
        try {
            PropertyUtils.getInstance("/tmp/config/iast.properties");
//            AgentRegisterReport.send();

            ReportSender report1 = new ReportSender();
            report1.send();
        } catch (Exception e) {
            System.err.println("HeartBeatReportTest testSend error " + e.getStackTrace());
        }
    }
}
