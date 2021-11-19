package com.secnium.iast.agent;

import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.monitor.EngineMonitor;
import com.secnium.iast.agent.monitor.MonitorDaemonThread;
import com.secnium.iast.agent.report.AgentRegisterReport;
import com.secnium.iast.agent.util.LogUtils;

import java.lang.instrument.Instrumentation;

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
     * @param featureString boot args [namespace,token,ip,port,prop]
     * @param inst          inst
     */
    public static void agentmain(String featureString, Instrumentation inst) {
        if ("uninstall".equals(featureString)) {
            LogUtils.info("Engine is about to be uninstalled");
            uninstall();
        } else {
            LAUNCH_MODE = LAUNCH_MODE_ATTACH;
            try {
                Agent.appendToolsPath();
                install(inst);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        IastProperties iastProperties = IastProperties.getInstance();
        Boolean send = AgentRegisterReport.send();
        if (send){
            LogUtils.info("Agent has successfully registered with "+iastProperties.getBaseUrl());
            Boolean agentStat = AgentRegisterReport.agentStat();
            if (!agentStat) {
                EngineMonitor.isCoreRegisterStart = false;
                LogUtils.info("The agent was not audited. Disable enabling.");
            }else {
                EngineMonitor.isCoreRegisterStart = true;
            }
            loadEngine(inst);
        }
    }

    private static void loadEngine(final Instrumentation inst) {
        EngineManager engineManager = EngineManager
                .getInstance(inst, LAUNCH_MODE, EngineManager.getPID());
        MonitorDaemonThread daemonThread = new MonitorDaemonThread(engineManager);
        Thread agentMonitorDaemonThread = new Thread(daemonThread);
        if (EngineMonitor.isCoreRegisterStart){
            daemonThread.startEngine();
        }
        agentMonitorDaemonThread.setDaemon(true);
        agentMonitorDaemonThread.setPriority(1);
        agentMonitorDaemonThread.setName("dongtai-monitor");
        agentMonitorDaemonThread.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }
}
