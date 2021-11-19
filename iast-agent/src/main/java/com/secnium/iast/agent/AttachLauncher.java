package com.secnium.iast.agent;

import com.secnium.iast.agent.util.JavaVersionUtils;
import com.secnium.iast.agent.util.LogUtils;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import java.io.IOException;
import java.util.Properties;

/**
 * @author 代码参考自开源项目jvm-sandbox
 */
public class AttachLauncher {

    private static final String AGENT_PATH = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    public static void attach(String pid, String args) throws Exception {
        VirtualMachine vmObj = null;
        try {
            LogUtils.info("trying attach to process " + pid + ", agent address is " + AGENT_PATH);
            vmObj = VirtualMachine.attach(pid);
            Properties targetSystemProperties = vmObj.getSystemProperties();
            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
            if (targetJavaVersion != null && currentJavaVersion != null) {
                if (!targetJavaVersion.equals(currentJavaVersion)) {
                    LogUtils.error(
                            "Current VM java version: " + currentJavaVersion + " do not match target VM java version: "
                                    + targetJavaVersion + ", attach may fail.");
                    LogUtils.error(
                            "Target VM JAVA_HOME is " + targetSystemProperties.getProperty("java.home")
                                    + ", DongTai-Agent JAVA_HOME is " + System.getProperty("java.home")
                                    + ", try to set the same JAVA_HOME.");
                }
            }

            vmObj.loadAgent(AGENT_PATH, "token=" + args);
            LogUtils.info("attach to process " + pid + " success.");
        } finally {
            if (null != vmObj) {
                vmObj.detach();
            }
        }
    }

    public static void detach(String pid) {
        VirtualMachine vmObj = null;
        try {
            vmObj = VirtualMachine.attach(pid);
            vmObj.detach();
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
