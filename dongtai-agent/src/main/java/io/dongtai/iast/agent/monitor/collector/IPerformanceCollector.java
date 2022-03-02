package io.dongtai.iast.agent.monitor.collector;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

/**
 * iperformance收集器
 * 性能监控收集器接口
 *
 * @author chenyi
 * @date 2022/2/28
 */
public interface IPerformanceCollector {

    /**
     * 获取性能指标
     *
     * @return 性能指标
     */
    PerformanceMetrics getMetrics();

    /**
     * 构建指标数据
     *
     * @param metricsKey   指标的名称枚举
     * @param metricsValue 指标值
     * @return {@link PerformanceMetrics}
     */
    PerformanceMetrics buildMetricsData(MetricsKey metricsKey, Object metricsValue);

}
