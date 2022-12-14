package io.dongtai.iast.agent.fallback.checker.impl;

import io.dongtai.iast.agent.fallback.FallbackConfig;
import io.dongtai.iast.agent.fallback.checker.IPerformanceChecker;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.util.List;
import java.util.Properties;


/**
 * 基础使用率检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public abstract class BasePerformanceChecker implements IPerformanceChecker {
    @Override
    public PerformanceMetrics getMatchMaxThreshold(MetricsKey metrics, Properties cfg) {
        final List<PerformanceMetrics> performanceLimitRiskThreshold = FallbackConfig.getPerformanceLimitMaxThreshold(cfg);
        for (PerformanceMetrics riskThreshold : performanceLimitRiskThreshold) {
            if (riskThreshold.getMetricsKey() == metrics) {
                return riskThreshold;
            }
        }
        return null;
    }
}
