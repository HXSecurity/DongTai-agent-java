package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.IastProperties;
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
        if (startEngine()) {
            while (true) {
                for (IMonitor monitor : this.monitorTasks) {
                    monitor.check();
                }
                threadSleep();
            }
        }
    }

    private boolean startEngine() {
        int timeInterval = properties.getDelayTime();
        if (timeInterval > 0) {
            try {
                System.out.println("开启延时加载模式，检测引擎将在" + timeInterval + "s后启动");
                Thread.sleep(timeInterval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        boolean status = engineManager.downloadEnginePackage();
        status = status && engineManager.install();
        status = status && engineManager.start();
        System.out.println("引擎启动成功，漏洞检测功能开启");
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
