package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.lang.management.ManagementFactory;

/**
 * 堆外内存使用率收集器
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class MemNoHeapUsageCollector extends AbstractPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        MemoryUsageMetrics metricsValue = MemoryUsageMetrics.clone(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        return buildMetricsData(MetricsKey.MEM_NO_HEAP_USAGE, metricsValue);
    }
}
