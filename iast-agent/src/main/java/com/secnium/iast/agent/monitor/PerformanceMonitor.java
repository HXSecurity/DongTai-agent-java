package com.secnium.iast.agent.monitor;

import com.secnium.iast.agent.manager.EngineManager;

/**
 * 负责监控jvm性能状态，如果达到停止阈值，则停止检测引擎；如果达到卸载阈值，则卸载引擎；
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PerformanceMonitor implements IMonitor {
    private final EngineManager engineManager;

    public PerformanceMonitor(EngineManager engineManager) {
        this.engineManager = engineManager;
    }


    public double memUsedRate() {
        double free = (double) Runtime.getRuntime().freeMemory();
        double max = (double) Runtime.getRuntime().maxMemory();
        return free / max;
    }

    /**
     * 是否到达停止引擎的阈值
     * // 内存：<80 -> >80%
     * // 前置状态：2
     * // 切换状态：3
     *
     * @return true, 需要停止；false - 不需要停止
     */
    public boolean isStop(double unUsedRate, int preStatus) {
        return unUsedRate < 0.2 && (preStatus == 2);
    }

    /**
     * 是否到达启动引擎的阈值
     * // 内存：>80% -> <80%
     * // 前置状态：3/1
     * // 切换状态：2
     *
     * @return true, 需要启动；false - 不需要启动
     */
    public boolean isStart(double unUsedRate, int preStatus) {
        return unUsedRate > 0.2 && (preStatus == 1 || preStatus == 3);
    }

    /**
     * 是否达到卸载引擎的阈值
     * // 内存：>80% -> >90%
     * // 前置状态：3
     * // 切换状态：4
     *
     * @return true, 需要卸载；false - 不需要卸载；
     */
    public boolean isUninstall(double unUsedRate, int preStatus) {
        return unUsedRate < 0.1 && (preStatus == 3);
    }

    /**
     * 是否达到安装引擎的阈值
     * // 内存：>90% -> >80%
     * // 前置状态：4
     * // 切换状态：1
     *
     * @return true, 需要安装；false - 不需要安装
     */
    public boolean isInstall(double unUsedRate, int preStatus) {
        return unUsedRate > 0.1 && (preStatus == 4);
    }

    /**
     * 状态发生转换时，触发engineManager的操作
     * <p>
     * 状态维护：
     * 0 -> 1 -> 2 -> 3 -> 4
     */
    @Override
    public void check() {
        double unUsedRate = memUsedRate();
        int preStatus = this.engineManager.getRunningStatus();
        if (isStart(unUsedRate, preStatus)) {
            this.engineManager.start();
            this.engineManager.setRunningStatus(2);
        } else if (isStop(unUsedRate, preStatus)) {
            this.engineManager.stop();
            this.engineManager.setRunningStatus(3);
        } else if (isUninstall(unUsedRate, preStatus)) {
            this.engineManager.uninstall();
            this.engineManager.setRunningStatus(4);
        } else if (isInstall(unUsedRate, preStatus)) {
            this.engineManager.install();
            this.engineManager.setRunningStatus(1);
        }
    }
}
