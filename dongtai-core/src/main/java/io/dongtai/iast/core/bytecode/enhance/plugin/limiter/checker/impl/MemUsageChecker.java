package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;

import java.util.Properties;


/**
 * 内存使用率检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class MemUsageChecker extends BasePerformanceChecker {

    @Override
    public boolean isPerformanceRisk(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchRiskThreshold(nowMetrics.getMetricsKey(), cfg);
        if (thresholdMetrics != null) {
            final MemoryUsageMetrics threshold = thresholdMetrics.getMetricsValue(MemoryUsageMetrics.class);
            final MemoryUsageMetrics now = nowMetrics.getMetricsValue(MemoryUsageMetrics.class);
            // 内存使用率
            if (threshold.getMemUsagePercentage() != null) {
                return now.getMemUsagePercentage() >= threshold.getMemUsagePercentage();
            }
            // 已用内存大小
            if (threshold.getUsed() != null) {
                return now.getUsed() >= threshold.getUsed();
            }
        }
        return false;
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(nowMetrics.getMetricsKey(), cfg);
        if (thresholdMetrics != null) {
            final MemoryUsageMetrics threshold = thresholdMetrics.getMetricsValue(MemoryUsageMetrics.class);
            final MemoryUsageMetrics now = nowMetrics.getMetricsValue(MemoryUsageMetrics.class);
            // 内存使用率
            if (threshold.getMemUsagePercentage() != null) {
                return now.getMemUsagePercentage() >= threshold.getMemUsagePercentage();
            }
            // 已用内存大小
            if (threshold.getUsed() != null) {
                return now.getUsed() >= threshold.getUsed();
            }
        }
        return false;
    }
}
