package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.*;
import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.log.DongTaiLog;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {

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
        monitorTasks.add(new ServerConfigMonitor());
        monitorTasks.add(new PerformanceMonitor(engineManager));
        monitorTasks.add(new EngineMonitor(engineManager));
        monitorTasks.add(new HeartBeatMonitor());
        monitorTasks.add(new SecondFallbackMonitor(engineManager));
        monitorTasks.add(new DongTaiThreadMonitor());
        this.engineManager = engineManager;
        try {
            delayTime = Integer.parseInt(System.getProperty("iast.engine.delay.time", "0"));
            DongTaiLog.info("dongtai engine delay time is " + delayTime + " s");
            delayTime = delayTime * 1000;
        } catch (Exception e) {
            DongTaiLog.error("engine delay time must be int,eg: 10、20");
            delayTime = 0;
        }
    }

    @Override
    public void run() {
        if (MonitorDaemonThread.delayTime > 0) {
            try {
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                DongTaiLog.error(e);
            }
            if (EngineMonitor.isCoreRegisterStart) {
                startEngine();
            }
        }
        // 引擎启动成功后，创建子线程执行monitor任务
        if(engineStartSuccess){
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
        boolean status = couldInstallEngine();
        // todo: 下载功能优先走本地缓存
        status = status && engineManager.extractPackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
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
