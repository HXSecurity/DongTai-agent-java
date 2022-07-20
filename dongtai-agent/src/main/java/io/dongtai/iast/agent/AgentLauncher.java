package io.dongtai.iast.agent;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.MonitorDaemonThread;
import io.dongtai.iast.agent.monitor.impl.EngineMonitor;
import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.iast.agent.util.ThreadUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;

import static io.dongtai.iast.agent.Agent.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentLauncher {

    public static final String LAUNCH_MODE_AGENT = "agent";
    public static final String LAUNCH_MODE_ATTACH = "attach";
    public static String LAUNCH_MODE;
    private static String FLUENT_FILE;
    private static String FLUENT_FILE_CONF;

    static {
        /**
         * fix bug: agent use sun.net.http, then allowRestrictedHeaders is false, so some custom server has wrong
         */
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        /**
         * fix bug: java.lang.ClassCastException: weblogic.net.http.SOAPHttpsURLConnection cannot be cast to javax.net.ssl.HttpsURLConnection
         */
        System.setProperty("UseSunHttpHandler", "true");
        if (System.getProperty("java.io.tmpdir.dongtai") == null) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            String appName = System.getProperty("dongtai.app.name");
            String appVersion = System.getProperty("dongtai.app.version");
            if (tmpdir == null) {
                tmpdir = File.separator + "tmp";
            }
            if (appName == null) {
                appName = "DemoProject";
            }
            if (appVersion == null) {
                appVersion = "v1.0.0";
            }
            System.setProperty("java.io.tmpdir.dongtai", tmpdir + File.separator + appName + "-" + appVersion + "-" + UUID.randomUUID().toString().replaceAll("-", "") + File.separator);
        }
    }

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
        LAUNCH_MODE = LAUNCH_MODE_AGENT;
        try {
            install(inst);
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
    }

    /**
     * install agent with attach
     *
     * @param args boot args [namespace,token,ip,port,prop]
     * @param inst inst
     */
    public static void agentmain(String args, Instrumentation inst) {
        Map<String, String> argsMap = parseArgs(args);
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
            if (argsMap.containsKey("clusterName")) {
                System.setProperty("dongtai.cluster.name", argsMap.get("clusterName"));
            }
            if (argsMap.containsKey("clusterVersion")) {
                System.setProperty("dongtai.cluster.version", argsMap.get("clusterVersion"));
            }
            if (argsMap.containsKey("dongtaiServer")) {
                System.setProperty("dongtai.server.url", argsMap.get("dongtaiServer"));
            }
            if (argsMap.containsKey("dongtaiToken")) {
                System.setProperty("dongtai.server.token", argsMap.get("dongtaiToken"));
            }
            if (argsMap.containsKey("serverPackage")) {
                System.setProperty("dongtai.server.package", argsMap.get("serverPackage"));
            }
            if (argsMap.containsKey("logLevel")) {
                System.setProperty("dongtai.log.level", argsMap.get("logLevel"));
            }
            if (argsMap.containsKey("logPath")) {
                System.setProperty("dongtai.log.path", argsMap.get("logPath"));
            }
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
        String tmpdir = System.getProperty("java.io.tmpdir");
        String appName = System.getProperty("dongtai.app.name");
        String appVersion = System.getProperty("dongtai.app.version");
        System.setProperty("java.io.tmpdir.dongtai", tmpdir + File.separator + appName + "-" + appVersion + "-" + UUID.randomUUID().toString().replaceAll("-", "") + File.separator);
        DongTaiLog.info("Protect By DongTai IAST: " + System.getProperty("protect.by.dongtai", "false"));
        if ("uninstall".equals(argsMap.get("mode"))) {
            if (System.getProperty("protect.by.dongtai", null) == null) {
                DongTaiLog.info("DongTai wasn't installed.");
                return;
            }
            EngineMonitor.setIsUninstallHeart(true);
            DongTaiLog.info("Engine is about to be uninstalled");
            uninstall();
            // attach手动卸载后停止守护线程
            ThreadUtils.killAllDongTaiThreads();
            System.clearProperty("protect.by.dongtai");
        } else {
            if (System.getProperty("protect.by.dongtai", null) != null) {
                DongTaiLog.info("DongTai already installed.");
                return;
            }
            MonitorDaemonThread.isExit = false;
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
        Boolean send = AgentRegisterReport.send();
        if (send) {
            extractFluent();
            DongTaiLog.info("Agent registered successfully.");
            Boolean agentStat = AgentRegisterReport.agentStat();
            if (!agentStat) {
                EngineMonitor.isCoreRegisterStart = false;
                DongTaiLog.info("Detection engine not started, agent waiting to be audited.");
            } else {
                EngineMonitor.isCoreRegisterStart = true;
            }
            Runtime.getRuntime().addShutdownHook(new ShutdownThread());
            loadEngine(inst);
            System.setProperty("protect.by.dongtai", "true");
        } else {
            DongTaiLog.error("Agent registered failed. Start without DongTai IAST.");
        }
    }

    private static void doFluent() {
        String[] execution = {
                "nohup",
                FLUENT_FILE,
                "-c",
                FLUENT_FILE_CONF
        };
        try {
            final Process process = Runtime.getRuntime().exec(execution);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    process.destroy();
                }
            }));
        } catch (IOException e) {
            DongTaiLog.error(e);
        }
    }

    private static void extractFluent() {
        try {
            if (!isMacOs() && !isWindows()) {
                FLUENT_FILE = System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "fluent";
                FileUtils.getResourceToFile("bin/fluent", FLUENT_FILE);

                FLUENT_FILE_CONF = System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "fluent.conf";
                FileUtils.getResourceToFile("bin/fluent.conf", FLUENT_FILE_CONF);
                FileUtils.confReplace(FLUENT_FILE_CONF);
                if ((new File(FLUENT_FILE)).setExecutable(true)) {
                    DongTaiLog.info("fluent extract success.");
                } else {
                    DongTaiLog.info("fluent extract failure. please set execute permission, file: {}", FLUENT_FILE);
                }
                doFluent();
            }
        } catch (IOException e) {
            DongTaiLog.error(e);
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
        agentMonitorDaemonThread.setName(Constant.THREAD_PREFIX + "MonitorDaemon");
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
