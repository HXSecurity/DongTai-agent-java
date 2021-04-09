package com.secnium.iast.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * @author 代码参考自开源项目jvm-sandbox
 */
public class AttachLauncher {
    private static String agentPath = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    public static void attach(String pid, String args) {
        VirtualMachine vmObj = null;
        try {
            System.out.println("[cn.huoxian.dongtai.iast] trying attach dongtai to process " + pid + ", agent address is " + agentPath);
            vmObj = VirtualMachine.attach(pid);
            vmObj.loadAgent(agentPath, "token=" + args);
            System.out.println("[cn.huoxian.dongtai.iast] attach dongtai to process " + pid + " success.");
        } catch (AttachNotSupportedException e) {
            System.err.println("[cn.huoxian.dongtai.iast] attach failed");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("[cn.huoxian.dongtai.iast] attach failed");
            e.printStackTrace();
        } catch (AgentLoadException e) {
            System.err.println("[cn.huoxian.dongtai.iast] attach failed");
            e.printStackTrace();
        } catch (AgentInitializationException e) {
            System.err.println("[cn.huoxian.dongtai.iast] attach failed");
            e.printStackTrace();
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
