package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.agent.monitor.collector.IPerformanceCollector;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;

import java.lang.management.ManagementFactory;

/**
 * 堆内存使用率收集器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class MemUsageCollector implements IPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        MemoryUsageMetrics memoryUsageMetrics = MemoryUsageMetrics.clone(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        final PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setMetricsKey(MetricsKey.MEM_USAGE);
        metrics.setMetricsValue(memoryUsageMetrics);
        return metrics;
    }
}
