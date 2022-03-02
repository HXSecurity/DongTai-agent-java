package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.lang.management.ManagementFactory;

/**
 * 堆内存使用率收集器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class MemUsageCollector extends AbstractPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        MemoryUsageMetrics metricsValue = MemoryUsageMetrics.clone(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        return buildMetricsData(MetricsKey.MEM_USAGE, metricsValue);
    }
}
