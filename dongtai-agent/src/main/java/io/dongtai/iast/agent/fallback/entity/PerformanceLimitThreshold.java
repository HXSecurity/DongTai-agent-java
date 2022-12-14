package io.dongtai.iast.agent.fallback.entity;

import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;

/**
 * 性能限制阈值配置
 *
 * @author chenyi
 * @date 2022/03/10
 */
public class PerformanceLimitThreshold {

    /**
     * cpu使用率阈值配置
     */
    private CpuInfoMetrics cpuUsage;
    /**
     * 内存使用率阈值配置
     */
    private MemoryUsageMetrics memoryUsage;

    public CpuInfoMetrics getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(CpuInfoMetrics cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public MemoryUsageMetrics getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(MemoryUsageMetrics memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
}
