package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.manager.EngineManager;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {
    public ArrayList<IMonitor> monitorTasks;

    public MonitorDaemonThread(EngineManager engineManager) {
        this.monitorTasks = new ArrayList<IMonitor>();
        this.monitorTasks.add(new PerformanceMonitor(engineManager));
        this.monitorTasks.add(new UpdateMonitor(engineManager));
        this.monitorTasks.add(new EngineMonitor(engineManager));
    }

    @Override
    public void run() {
        while (true) {
            for (IMonitor monitor : this.monitorTasks) {
                monitor.check();
            }
            threadSleep();
        }
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
