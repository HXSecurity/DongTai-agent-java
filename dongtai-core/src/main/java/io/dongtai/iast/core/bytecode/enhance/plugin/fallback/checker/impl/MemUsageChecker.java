package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.impl;

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
        return checkIsMemUsageOverThreshold(nowMetrics, thresholdMetrics);
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(nowMetrics.getMetricsKey(), cfg);
        return checkIsMemUsageOverThreshold(nowMetrics, thresholdMetrics);
    }

    private boolean checkIsMemUsageOverThreshold(PerformanceMetrics nowMetrics, PerformanceMetrics thresholdMetrics) {
        if (thresholdMetrics != null) {
            final MemoryUsageMetrics threshold = thresholdMetrics.getMetricsValue(MemoryUsageMetrics.class);
            final MemoryUsageMetrics now = nowMetrics.getMetricsValue(MemoryUsageMetrics.class);
            // 内存使用率
            if (threshold.getMemUsagePercentage() != null
                    && now.getMemUsagePercentage() >= threshold.getMemUsagePercentage()) {
                return true;
            }
            // 已用内存大小
            return threshold.getUsed() != null && now.getUsed() >= threshold.getUsed();
        }
        return false;
    }
}
