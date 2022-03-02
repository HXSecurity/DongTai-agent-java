package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.agent.monitor.collector.IPerformanceCollector;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * 垃圾回收信息收集器
 *
 * @author chenyi
 * @date 2022/3/1
 */
public class GarbageInfoCollector implements IPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        final GarbageInfoMetrics garbageInfoMetrics = new GarbageInfoMetrics();
        // 获取当前有效的垃圾收集器
        final List<GarbageCollectorMXBean> garbageBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean each : garbageBeans) {
            if (each.isValid()) {
                garbageInfoMetrics.addCollectionInfo(each.getName(), each.getCollectionCount(), each.getCollectionTime());
            }

        }

        final PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setMetricsKey(MetricsKey.GARBAGE_INFO);
        metrics.setMetricsValue(garbageInfoMetrics);
        return metrics;
    }
}
