package io.dongtai.iast.agent.monitor.collector.impl;

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
public class GarbageInfoCollector extends AbstractPerformanceCollector {

    @Override
    public PerformanceMetrics getMetrics() {
        final GarbageInfoMetrics metricsValue = new GarbageInfoMetrics();
        // 获取当前有效的垃圾收集器
        final List<GarbageCollectorMXBean> garbageBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean each : garbageBeans) {
            if (each.isValid()) {
                metricsValue.addCollectionInfo(each.getName(), each.getCollectionCount(), each.getCollectionTime());
            }
        }
        return buildMetricsData(MetricsKey.GARBAGE_INFO, metricsValue);
    }
}
