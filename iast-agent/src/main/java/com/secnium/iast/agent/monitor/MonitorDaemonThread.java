package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.manager.EngineManager;
import com.secnium.iast.agent.util.LogUtils;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {

    public ArrayList<IMonitor> monitorTasks;
    public static boolean isExit = false;
    private final EngineManager engineManager;

    public MonitorDaemonThread(EngineManager engineManager) {
        this.monitorTasks = new ArrayList<IMonitor>();
        this.monitorTasks.add(new PerformanceMonitor(engineManager));
        this.monitorTasks.add(new EngineMonitor(engineManager));
        this.monitorTasks.add(new HeartBeatMonitor());
        this.engineManager = engineManager;
    }

    @Override
    public void run() {
        while (!isExit) {
            for (IMonitor monitor : this.monitorTasks) {
                monitor.check();
            }
            threadSleep();
        }
    }

    private void threadSleep() {
        try {
            long milliseconds = 10 * 1000L;
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void startEngine() {
        boolean status = engineManager.downloadEnginePackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        if (!status) {
            LogUtils.info("DongTai IAST started failure");
        }
    }
}
