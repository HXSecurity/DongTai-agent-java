package io.dongtai.iast.agent;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.EngineMonitor;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.log.DongTaiLog;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentLauncher {

    public static final String LAUNCH_MODE_AGENT = "agent";
    public static final String LAUNCH_MODE_ATTACH = "attach";
    public static String LAUNCH_MODE;

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
        DongTaiLog.info(System.getProperty("protect.by.dongtai", "Current Application Run Without DongTai"));
        Map<String, String> argsMap = parseArgs(args);
        if ("uninstall".equals(argsMap.get("mode"))) {
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
                if (argsMap.containsKey("debug")) {
                    System.setProperty("dongtai.debug", argsMap.get("debug"));
                }
                if (argsMap.containsKey("appCreate")) {
                    System.setProperty("dongtai.app.create", argsMap.get("appCreate"));
                }
                if (argsMap.containsKey("appName")) {
                    System.setProperty("dongtai.app.name", argsMap.get("appName"));
                }
                if (argsMap.containsKey("appVersion")) {
                    System.setProperty("dongtai.app.version", argsMap.get("appVersion"));
                }
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
        if (iastProperties == null){
            DongTaiLog.error("Insufficient Agent permissions, profile creation failed. Start without DongTai IAST.");
            return;
        }
        DongTaiLog.info("try to register agent to: " + iastProperties.getBaseUrl());
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
            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
            loadEngine(inst);
            System.setProperty("protect.by.dongtai", "1");
        } else {
            DongTaiLog.error("Agent register failed. Start without DongTai IAST.");
        }
    }

    private static void loadEngine(final Instrumentation inst) {
        EngineManager engineManager = EngineManager.getInstance(inst, LAUNCH_MODE, EngineManager.getPID());
        MonitorDaemonThread daemonThread = new MonitorDaemonThread(engineManager);
        Thread agentMonitorDaemonThread = new Thread(daemonThread);
        if (MonitorDaemonThread.delayTime <= 0 && EngineMonitor.isCoreRegisterStart) {
            daemonThread.startEngine();
        }

        agentMonitorDaemonThread.setDaemon(true);
        agentMonitorDaemonThread.setPriority(1);
        agentMonitorDaemonThread.setName("dongtai-monitor");
        agentMonitorDaemonThread.start();
    }

    private static Map<String, String> parseArgs(String args) {
        Map<String, String> argsMap = new HashMap<String, String>();
        String[] argsItems = args.split("&");
        for (String argsItem : argsItems) {
            String[] argItems = argsItem.split("=");
            argsMap.put(argItems[0], argItems[1]);
        }
        return argsMap;
    }
}
