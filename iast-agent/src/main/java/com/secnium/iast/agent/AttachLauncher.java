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
    private static final String AGENT_PATH = Agent.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    public static void attach(String pid, String args) {
        VirtualMachine vmObj = null;
        try {
            LogUtils.info("trying attach to process " + pid + ", agent address is " + AGENT_PATH);
            vmObj = VirtualMachine.attach(pid);
            vmObj.loadAgent(AGENT_PATH, "token=" + args);
            LogUtils.info("attach to process " + pid + " success.");
            vmObj.detach();
        } catch (AttachNotSupportedException e) {
            LogUtils.error("attach failed, reason: Attach not support");
        } catch (IOException e) {
            LogUtils.info("attach to process " + pid + ", Please wait and check the WEB service log to observe whether the engine has started successfully");
        } catch (AgentLoadException e) {
            LogUtils.error("attach failed, reason: agent load error");
        } catch (AgentInitializationException e) {
            LogUtils.error("attach failed, reason: agent init error");
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
