package com.secnium.iast.agent;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import com.sun.management.OperatingSystemMXBean;
import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.report.AgentRegisterReport;
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

    @Test
    public void changeFile() {
        replace("/Users/erzhuangniu/workspace/DongTai-agent-java/dongtai-agent/src/main/resources/bin/fluent.conf");
    }

    public static void replace(String path) {
        String temp = "";

        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();
            // 保存该行前面的内容
            while ((temp = br.readLine()) != null) {
                if (temp.contains("${HOSTNAME_AGENT_ID}")){
                    temp.replace("${HOSTNAME_AGENT_ID}", AgentRegisterReport.getInternalHostName()+"-"+AgentRegisterReport.getAgentFlag().toString());
                }else if (temp.contains("${HOSTNAME}")){
                    temp.replace("${HOSTNAME}",AgentRegisterReport.getInternalHostName());
                }else if (temp.contains("${AGENT_ID}")){
                    temp.replace("${AGENT_ID}",AgentRegisterReport.getAgentFlag().toString());
                }else if (temp.contains("${OPENAPI}")){
                    temp.replace("${OPENAPI}", IastProperties.getInstance().getBaseUrl());
                }else if (temp.contains("${LOG_PORT}")){
                    temp.replace("${LOG_PORT}",IastProperties.getInstance().getLogPort());
                }else if (temp.contains("${LOG_PATH}")){
                    temp.replace("${LOG_PATH}", System.getProperty("dongtai.log.path")+File.separator+"dongtai_javaagent.log");
                }
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }
            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void doFluent() {
        String[] execution = {
                "nohup",
                "tail",
                "-f",
                "/var/folders/xy/xyx56h3s29z6376gvk32621h0000gn/T//gunsTest001-042401-8579dc8d088d4a1680977352f6652aba/iast/fluent.conf"
        };
        try {
            Runtime.getRuntime().exec(execution);
            System.out.println("aasdasdsa");
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    @Test
    public void doAaaa() {
        String s = "https://iast.io/openapi";
        int i = s.indexOf("://");
        int i1 = s.indexOf("/openapi");
        System.out.println();
    }

    public static void main(String[] args) {
        com.sun.management.OperatingSystemMXBean osmxb = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemorySize = osmxb.getTotalPhysicalMemorySize();
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        System.out.println(totalPhysicalMemorySize);
        System.out.println(freePhysicalMemorySize);
    }
}
