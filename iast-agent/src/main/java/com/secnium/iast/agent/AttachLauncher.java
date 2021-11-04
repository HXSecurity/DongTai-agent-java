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
            //attach连接上目标vm后，发送指令，让目标vm加载路径为 AGENT_PATH 的agent，可携带参数 token=xxxx
            vmObj.loadAgent(AGENT_PATH, "token=" + args);
            LogUtils.info("attach to process " + pid + " success.");
            //attach方式主要是发送指令给目标vm执行加载agent的指令，完成后就可以detach了
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
