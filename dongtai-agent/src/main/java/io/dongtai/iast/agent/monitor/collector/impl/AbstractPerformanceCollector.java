package io.dongtai.iast.agent.monitor.collector.impl;

import io.dongtai.iast.agent.monitor.collector.IPerformanceCollector;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.util.Date;

/**
 * 性能信息收集器抽象
 *
 * @author chenyi
 * @date 2022/3/1
 */
public abstract class AbstractPerformanceCollector implements IPerformanceCollector {

    @Override
    public PerformanceMetrics buildMetricsData(MetricsKey metricsKey, Object metricsValue) {
        final PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setMetricsKey(metricsKey);
        metrics.setCollectDate(new Date());
        metrics.setMetricsValue(metricsValue);
        return metrics;
    }

}
