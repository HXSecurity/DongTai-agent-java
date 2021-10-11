package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.IastProperties;
import com.secnium.iast.agent.LogUtils;
import com.secnium.iast.agent.manager.EngineManager;

import java.util.ArrayList;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class MonitorDaemonThread implements Runnable {
    public ArrayList<IMonitor> monitorTasks;
    private final EngineManager engineManager;
    private final IastProperties properties = IastProperties.getInstance();

    public MonitorDaemonThread(EngineManager engineManager) {
        this.monitorTasks = new ArrayList<IMonitor>();
        this.monitorTasks.add(new PerformanceMonitor(engineManager));
        this.monitorTasks.add(new EngineMonitor(engineManager));
        this.engineManager = engineManager;
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

    public boolean startEngine() {
        int timeInterval = properties.getDelayTime();
        if (timeInterval > 0) {
            try {
                Thread.sleep(timeInterval * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        boolean status = engineManager.downloadEnginePackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        if (status) {
            LogUtils.info("DongTai IAST started successfully");
        } else {
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
