package com.secnium.iast.agent;

import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.monitor.MonitorDaemonThread;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentLauncher {
    private static final String LAUNCH_MODE_AGENT = "agent";
    private static final String LAUNCH_MODE_ATTACH = "attach";
    private static String LAUNCH_MODE;

    /**
     * install agent with premain
     *
     * @param args boot args [namespace,token,ip,port,prop]
     * @param inst inst
     */
    public static void premain(String args, Instrumentation inst) {
        LAUNCH_MODE = LAUNCH_MODE_AGENT;
        try {
            Agent.appendToolsPath();
            install(inst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * install agent with attach
     *
     * @param featureString boot args
     *                      [namespace,token,ip,port,prop]
     * @param inst          inst
     */
    public static void agentmain(String featureString, Instrumentation inst) {
        if ("uninstall".equals(featureString)) {
            LogUtils.info("Engine is about to be uninstalled");
            uninstall();
        } else {
            LAUNCH_MODE = LAUNCH_MODE_ATTACH;
            install(inst);
        }
    }


    /**
     * uninstall agent
     */
    @SuppressWarnings("unused")
    public static synchronized void uninstall() {
        EngineManager engineManager = EngineManager.getInstance();
        engineManager.uninstall();
    }

    /**
     * install agent
     *
     * @param inst inst
     */
    private static void install(final Instrumentation inst) {
        IastProperties.getInstance();
        loadEngine(inst);
    }

    private static void loadEngine(final Instrumentation inst) {
        EngineManager engineManager = EngineManager.getInstance(inst, LAUNCH_MODE, ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        MonitorDaemonThread daemonThread = new MonitorDaemonThread(engineManager);
        if (daemonThread.startEngine()) {
            Thread agentMonitorDaemonThread = new Thread(daemonThread);
            agentMonitorDaemonThread.setDaemon(true);
            agentMonitorDaemonThread.setName("dongtai-agent-monitor");
            agentMonitorDaemonThread.start();
        }
    }
}
