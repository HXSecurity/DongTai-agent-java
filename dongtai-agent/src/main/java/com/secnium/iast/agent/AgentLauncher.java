package com.secnium.iast.agent;

import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.monitor.EngineMonitor;
import com.secnium.iast.agent.monitor.MonitorDaemonThread;
import com.secnium.iast.agent.report.AgentRegisterReport;
import com.secnium.iast.log.DongTaiLog;
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
        if (System.getProperty("protect.by.dongtai", null) != null) {
            return;
        }
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        LAUNCH_MODE = LAUNCH_MODE_AGENT;
        try {
            install(inst);
            System.setProperty("protect.by.dongtai", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * install agent with attach
     *
     * @param args boot args [namespace,token,ip,port,prop]
     * @param inst inst
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println(System.getProperty("protect.by.dongtai", null));
        if ("uninstall".equals(args)) {
            if (System.getProperty("protect.by.dongtai", null) == null) {
                DongTaiLog.info("DongTai wasn't installed.");
                return;
            }
            DongTaiLog.info("Engine is about to be uninstalled");
            uninstall();
            System.setProperty("protect.by.dongtai", null);
        } else {
            if (System.getProperty("protect.with.dongtai", null) != null) {
                DongTaiLog.info("DongTai already installed.");
                return;
            }
            LAUNCH_MODE = LAUNCH_MODE_ATTACH;
            try {
                Agent.appendToolsPath();
                install(inst);
                System.setProperty("protect.with.dongtai", "1");
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
        if (send) {
            DongTaiLog.info("Agent has successfully registered with " + iastProperties.getBaseUrl());
            Boolean agentStat = AgentRegisterReport.agentStat();
            if (!agentStat) {
                EngineMonitor.isCoreRegisterStart = false;
                DongTaiLog.info("The agent was not audited. Disable enabling.");
            } else {
                EngineMonitor.isCoreRegisterStart = true;
            }
            loadEngine(inst);
        } else {
            DongTaiLog.error("Agent register failed. Start without DongTai IAST.");
        }
    }

    private static void loadEngine(final Instrumentation inst) {
        EngineManager engineManager = EngineManager
                .getInstance(inst, LAUNCH_MODE, EngineManager.getPID());
        MonitorDaemonThread daemonThread = new MonitorDaemonThread(engineManager);
        Thread agentMonitorDaemonThread = new Thread(daemonThread);
        if (MonitorDaemonThread.delayTime <= 0 && EngineMonitor.isCoreRegisterStart) {
            daemonThread.startEngine();
        }

        agentMonitorDaemonThread.setDaemon(true);
        agentMonitorDaemonThread.setPriority(1);
        agentMonitorDaemonThread.setName("dongtai-monitor");
        agentMonitorDaemonThread.start();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }
}
