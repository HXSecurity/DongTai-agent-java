package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.LogUtils;
import com.secnium.iast.agent.manager.EngineManager;
import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {

    public ArrayList<IMonitor> monitorTasks;
    private final EngineManager engineManager;
    public static boolean isExit = false;

    public MonitorDaemonThread(EngineManager engineManager) {
        this.monitorTasks = new ArrayList<IMonitor>();
        this.monitorTasks.add(new PerformanceMonitor(engineManager));
        this.monitorTasks.add(new EngineMonitor(engineManager));
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

    public boolean startEngine() {
        boolean status = engineManager.downloadEnginePackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        if (!status) {
            LogUtils.info("DongTai IAST started failure");
        }

        return status;
    }

    private void threadSleep() {
        try {
            long milliseconds = 60 * 1000L;
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
