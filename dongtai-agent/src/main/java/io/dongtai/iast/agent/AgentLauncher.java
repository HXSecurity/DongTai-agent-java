package io.dongtai.iast.agent;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.common.constants.AgentConstant;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.common.state.*;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

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
    private static Thread shutdownHook;
    private static Thread agentMonitorDaemonThread = null;
    private static final AgentState AGENT_STATE = AgentState.getInstance();

    static {
        /**
         * fix bug: agent use sun.net.http, then allowRestrictedHeaders is false, so some custom server has wrong
         */
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        /**
         * fix bug: java.lang.ClassCastException: weblogic.net.http.SOAPHttpsURLConnection cannot be cast to javax.net.ssl.HttpsURLConnection
         */
        System.setProperty("UseSunHttpHandler", "true");
    }

    /**
     * install agent with premain
     *
     * @param args boot args [namespace,token,ip,port,prop]
     * @param inst inst
     */
    public static void premain(String args, Instrumentation inst) {
        LAUNCH_MODE = LAUNCH_MODE_AGENT;
        AGENT_STATE.setPendingState(State.RUNNING);
        initLogger();
        try {
            IastProperties.getInstance();
            install(inst);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.AGENT_PREMAIN_INVOKE_FAILED, e);
        } finally {
            AGENT_STATE.setPendingState(null);
        }
    }

    /**
     * install agent with attach
     *
     * @param args boot args [namespace,token,ip,port,prop]
     * @param inst inst
     */
    public static void agentmain(String args, Instrumentation inst) {
        String mode;
        try {
            Map<String, String> argsMap = parseArgs(args);
            mode = argsMap.get("mode");
            for (String prop : IastProperties.ATTACH_ARG_MAP.values()) {
                if (argsMap.containsKey(prop)) {
                    System.setProperty(prop, argsMap.get(prop));
                }
            }
        } catch (Throwable e) {
            AGENT_STATE.setState(State.EXCEPTION);
            DongTaiLog.error(ErrorCode.AGENT_ATTACH_PARSE_ARGS_FAILED, e);
            return;
        }

        initLogger();
        IastProperties.getInstance();
        StateCause cause = null;

        if ("uninstall".equals(mode)) {
            cause = StateCause.UNINSTALL_BY_CLI;
            if (AGENT_STATE.getPendingState() != null) {
                DongTaiLog.info("DongTai agent uninstall: " + AGENT_STATE.getPendingStateInfo());
                return;
            }
            if (!AgentState.getInstance().isInit() || AGENT_STATE.isUninstalled()) {
                DongTaiLog.info("DongTai wasn't installed.");
                return;
            }

            try {
                AGENT_STATE.setPendingState(State.UNINSTALLED);

                DongTaiLog.info("Engine is about to be uninstalled");
                uninstall();
                // attach手动卸载后停止守护线程
                if (shutdownHook != null) {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                    shutdownHook = null;
                }
                ScopeManager.SCOPE_TRACKER.remove();

                if (!AGENT_STATE.isException()) {
                    AGENT_STATE.setState(State.UNINSTALLED);
                }
                AGENT_STATE.setCause(cause);
            } catch (Throwable e) {
                AGENT_STATE.setState(State.EXCEPTION).setCause(cause);
                DongTaiLog.error(ErrorCode.AGENT_ATTACH_UNINSTALL_FAILED, e);
            } finally {
                AGENT_STATE.setPendingState(null);
            }

            return;
        }

        // install
        cause = StateCause.RUNNING_BY_CLI;
        if (AGENT_STATE.getPendingState() != null) {
            DongTaiLog.info("DongTai agent install: " + AGENT_STATE.getPendingStateInfo());
            return;
        }
        if (AGENT_STATE.isRunning()) {
            DongTaiLog.info("DongTai already installed.");
            return;
        }

        try {
            AGENT_STATE.setPendingState(State.RUNNING);

            MonitorDaemonThread.isExit = false;
            LAUNCH_MODE = LAUNCH_MODE_ATTACH;
            install(inst);
        } catch (Throwable e) {
            AGENT_STATE.setState(State.EXCEPTION).setCause(cause);
            DongTaiLog.error(ErrorCode.AGENT_ATTACH_INSTALL_FAILED, e);
        } finally {
            AGENT_STATE.setPendingState(null);
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
        Boolean send = AgentRegisterReport.send();
        if (send) {
            LogCollector.extractFluent();
            DongTaiLog.info("Agent registered successfully.");
            shutdownHook = new ShutdownThread();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            loadEngine(inst);
            if (!AGENT_STATE.isException()) {
                AGENT_STATE.setState(State.RUNNING);
            }
        } else {
            DongTaiLog.error(ErrorCode.AGENT_REGISTER_INFO_INVALID);
            AGENT_STATE.setState(State.EXCEPTION);
        }
        AGENT_STATE.setCause(StateCause.RUNNING_BY_CLI);
    }

    private static void loadEngine(final Instrumentation inst) {
        EngineManager engineManager = EngineManager.getInstance(inst, LAUNCH_MODE, EngineManager.getPID(), AGENT_STATE);
        MonitorDaemonThread daemonThread = MonitorDaemonThread.getInstance(engineManager);
        if (MonitorDaemonThread.delayTime <= 0) {
            daemonThread.startEngine();
        }

        if (agentMonitorDaemonThread == null) {
            agentMonitorDaemonThread = new Thread(daemonThread);
            agentMonitorDaemonThread.setDaemon(true);
            agentMonitorDaemonThread.setPriority(1);
            agentMonitorDaemonThread.setName(AgentConstant.THREAD_NAME_PREFIX + "MonitorDaemon");
            agentMonitorDaemonThread.start();
        }
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

    private static void initLogger() {
        try {
            IastProperties.initTmpDir();
            DongTaiLog.configure(AgentRegisterReport.getAgentId());
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.LOG_INITIALIZE_FAILED, e);
        }
    }
}
