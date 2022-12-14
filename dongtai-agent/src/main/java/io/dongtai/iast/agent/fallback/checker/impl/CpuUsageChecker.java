package io.dongtai.iast.agent.fallback.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;

import java.util.Properties;


/**
 * cpu使用率检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class CpuUsageChecker extends BasePerformanceChecker {
    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(nowMetrics.getMetricsKey(), cfg);
        if (thresholdMetrics != null) {
            final CpuInfoMetrics threshold = thresholdMetrics.getMetricsValue(CpuInfoMetrics.class);
            final CpuInfoMetrics now = nowMetrics.getMetricsValue(CpuInfoMetrics.class);
            // cpu使用率
            if (threshold.getCpuUsagePercentage() != null) {
                return now.getCpuUsagePercentage() >= threshold.getCpuUsagePercentage();
            }
        }
        return false;
    }
}
