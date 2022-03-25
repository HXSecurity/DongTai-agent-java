package io.dongtai.iast.agent.monitor;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.*;
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
            DongTaiLog.info("engine delay time is " + delayTime + " s");
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
        // 启动子线程执行monitor任务.
        for (IMonitor monitor : monitorTasks) {
            Thread monitorThread = new Thread(monitor, monitor.getName());
            monitorThread.setDaemon(true);
            monitorThread.setPriority(1);
            monitorThread.start();
        }
    }

    //todo: 检测所有线程信息。


    public void startEngine() {
        // todo: 下载功能优先走本地缓存
        boolean status = engineManager.extractPackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        if (!status) {
            DongTaiLog.info("DongTai IAST started failure");
        }
    }
}
