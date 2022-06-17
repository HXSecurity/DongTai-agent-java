package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class SystemMemUsageCollector extends AbstractPerformanceCollector {
    /**
     * 获取性能指标
     *
     * @return 性能指标
     */
    @Override
    public PerformanceMetrics getMetrics() {
        com.sun.management.OperatingSystemMXBean osmxb = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemorySize = osmxb.getTotalPhysicalMemorySize();
        long usedPhysicalMemorySize = totalPhysicalMemorySize - osmxb.getFreePhysicalMemorySize();
        MemoryUsageMetrics metricsValue = MemoryUsageMetrics.clone(new MemoryUsage(totalPhysicalMemorySize, usedPhysicalMemorySize, usedPhysicalMemorySize, totalPhysicalMemorySize));
        return buildMetricsData(MetricsKey.MEM_NO_HEAP_USAGE, metricsValue);
    }
}
