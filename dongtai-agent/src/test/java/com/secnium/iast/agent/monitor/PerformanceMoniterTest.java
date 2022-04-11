package com.secnium.iast.agent.monitor;

import io.dongtai.iast.agent.manager.EngineManager;
import io.dongtai.iast.agent.monitor.impl.PerformanceMonitor;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class PerformanceMoniterTest {

    @Test
    public void memUsedRate() {
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance());
        System.out.println(performanceMoniter.memUsedRate());
    }

    @Test
    public void isStop() {
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance());
        double unUserdRate = performanceMoniter.memUsedRate();
        boolean status = performanceMoniter.isStop(unUserdRate, 2);
        System.out.println("PerformanceMoniterTest.isStop=" + status);
    }

    @Test
    public void isStart() {
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance());
        double unUserdRate = performanceMoniter.memUsedRate();
        boolean status = performanceMoniter.isStart(unUserdRate, 1);
        System.out.println("PerformanceMoniterTest.isStart=" + status);
    }

    @Test
    public void isUninstall() {
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance());
        double unUserdRate = performanceMoniter.memUsedRate();
//        boolean status = performanceMoniter.isUninstall(unUserdRate, 3);
//        System.out.println("PerformanceMoniterTest.isUninstall=" + status);
    }

    @Test
    public void isInstall() {
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance());
        double unUserdRate = performanceMoniter.memUsedRate();
//        boolean status = performanceMoniter.isInstall(unUserdRate, 4);
//        System.out.println("PerformanceMoniterTest.isInstall=" + status);
    }

    @Test
    public void check() throws Exception {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        PerformanceMonitor performanceMoniter = new PerformanceMonitor(EngineManager.getInstance(null, null, runtimeMXBean.getName().split("@")[0]));
        performanceMoniter.check();
        System.out.println(EngineManager.getInstance().getRunningStatus());
    }
}
