package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.IastProperties;
import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.*;
import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.log.DongTaiLog;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {
    private static MonitorDaemonThread instance;

    public static ArrayList<IMonitor> monitorTasks;
    public static boolean isExit = false;
    private final EngineManager engineManager;
    public static int delayTime = 0;
    /**
     * 引擎是否启动成功
     */
    public static boolean engineStartSuccess = false;

    public MonitorDaemonThread(EngineManager engineManager) {
        monitorTasks = new ArrayList<IMonitor>();
        monitorTasks.add(new FallbackConfigMonitor());
        monitorTasks.add(new ConfigMonitor());
        monitorTasks.add(new PerformanceMonitor(engineManager));
        monitorTasks.add(new AgentStateMonitor(engineManager));
        monitorTasks.add(new HeartBeatMonitor());
        this.engineManager = engineManager;
        try {
            delayTime = IastProperties.getInstance().getDelayTime();
            if (delayTime != 0) {
                DongTaiLog.info("dongtai engine delay time is " + delayTime + "s");
                delayTime = delayTime * 1000;
            }
        } catch (Throwable e) {
            DongTaiLog.info("engine delay time must be int, eg: 15");
            delayTime = 0;
        }
    }

    public static MonitorDaemonThread getInstance(EngineManager engineManager) {
        if (instance == null) {
            instance = new MonitorDaemonThread(engineManager);
        }
        return instance;
    }

    @Override
    public void run() {
        if (MonitorDaemonThread.delayTime > 0) {
            try {
                Thread.sleep(delayTime);
                startEngine();
            } catch (InterruptedException ignore) {
            }
        }
        // 引擎启动成功后，创建子线程执行monitor任务
        if (engineStartSuccess) {
            for (IMonitor monitor : monitorTasks) {
                Thread monitorThread = new Thread(monitor, monitor.getName());
                monitorThread.setDaemon(true);
                monitorThread.setPriority(1);
                monitorThread.start();
            }
        }
    }

    //todo: 检测所有线程信息。


    public void startEngine() {
        boolean status = false;
        if (couldInstallEngine()) {
            // jdk8以上
            status = engineManager.extractPackage();
            status = status && engineManager.install();
        }
        if (!status) {
            DongTaiLog.info("DongTai IAST started failure");
        }
        engineStartSuccess = status;
    }

    /**
     * 是否可以安装引擎
     *
     * @return boolean
     */
    private boolean couldInstallEngine() {
        // 低版本jdk暂不支持安装引擎core包
        if (JavaVersionUtils.isJava6() || JavaVersionUtils.isJava7()) {
            DongTaiLog.info("DongTai Engine core couldn't install because of low JDK version:" + JavaVersionUtils.javaVersionStr());
            return false;
        }
        return true;
    }

}
