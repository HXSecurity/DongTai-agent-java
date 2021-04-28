package com.secnium.iast.agent;

import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.monitor.MonitorDaemonThread;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class AgentLauncher {

    // 启动模式: agent方式加载
    private static final String LAUNCH_MODE_AGENT = "agent";

    // 启动模式: attach方式加载
    private static final String LAUNCH_MODE_ATTACH = "attach";

    // 启动默认
    private static String LAUNCH_MODE;

    /**
     * 启动加载
     *
     * @param args 启动参数 传入token、配置文件路径
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
     * 动态加载
     *
     * @param featureString 启动参数
     *                      [namespace,token,ip,port,prop]
     * @param inst          inst
     */
    public static void agentmain(String featureString, Instrumentation inst) {
        if ("uninstall".equals(featureString)) {
            System.out.println("[cn.huoxian.dongtai.iast] Engine is about to be uninstalled");
            uninstall();
        } else {
            LAUNCH_MODE = LAUNCH_MODE_ATTACH;
            install(inst);
        }
    }


    /**
     * 卸载agent
     */
    @SuppressWarnings("unused")
    public static synchronized void uninstall() {
        EngineManager engineManager = EngineManager.getInstance();
        engineManager.uninstall();
    }

    /**
     * 安装agent
     *
     * @param inst inst
     */
    private static void install(final Instrumentation inst) {
        long startTime = System.nanoTime();
        IastProperties.getInstance();
        EngineManager engineManager = EngineManager.getInstance(inst, LAUNCH_MODE, ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

        boolean status = engineManager.downloadEnginePackage();
        status = status && engineManager.install();
        status = status && engineManager.start();

        if (status) {
            Thread agentMonitorDaemonThead = new Thread(new MonitorDaemonThread(engineManager));
            agentMonitorDaemonThead.setDaemon(true);
            agentMonitorDaemonThead.setName("dongtai-agent-monitor");
            agentMonitorDaemonThead.start();

            System.out.println("[cn.huoxian.dongtai.iast] Successfully opened the engine, and it takes  " + (System.nanoTime() - startTime) / 1000 / 1000 / 1000 + "s");
        } else {
            System.out.println("[cn.huoxian.dongtai.iast] Engine start failed, start the application directly without starting the engine, and it takes  " + (System.nanoTime() - startTime) / 1000 / 1000 / 1000 + "s");
        }
    }


}
