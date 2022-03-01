package io.dongtai.iast.agent.monitor.collector;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;

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

}
