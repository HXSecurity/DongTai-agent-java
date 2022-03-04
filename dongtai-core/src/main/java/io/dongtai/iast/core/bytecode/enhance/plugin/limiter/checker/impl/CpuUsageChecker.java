package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker.IPerformanceChecker;
import io.dongtai.iast.core.utils.RemoteConfigUtils;

import java.util.List;
import java.util.Properties;


/**
 * cpu使用率检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class CpuUsageChecker implements IPerformanceChecker {
    @Override
    public boolean isPerformanceRisk(PerformanceMetrics performanceMetrics, Properties cfg) {
        final List<PerformanceMetrics> performanceLimitRiskThreshold = RemoteConfigUtils.getPerformanceLimitRiskThreshold(cfg);
        for (PerformanceMetrics riskThreshold : performanceLimitRiskThreshold) {
            if (riskThreshold.getMetricsKey() == performanceMetrics.getMetricsKey()) {
                final Double riskCpuUsagePercentage = riskThreshold.getMetricsValue(CpuInfoMetrics.class).getCpuUsagePercentage();
                final Double cpuUsagePercentage = performanceMetrics.getMetricsValue(CpuInfoMetrics.class).getCpuUsagePercentage();
                if (cpuUsagePercentage >= riskCpuUsagePercentage) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics performanceMetrics, Properties cfg) {
        final List<PerformanceMetrics> performanceLimitMaxThreshold = RemoteConfigUtils.getPerformanceLimitMaxThreshold(cfg);
        for (PerformanceMetrics maxThreshold : performanceLimitMaxThreshold) {
            if (maxThreshold.getMetricsKey() == performanceMetrics.getMetricsKey()) {
                final Double riskCpuUsagePercentage = maxThreshold.getMetricsValue(CpuInfoMetrics.class).getCpuUsagePercentage();
                final Double cpuUsagePercentage = performanceMetrics.getMetricsValue(CpuInfoMetrics.class).getCpuUsagePercentage();
                if (cpuUsagePercentage >= riskCpuUsagePercentage) {
                    return true;
                }
            }
        }
        return false;
    }
}
