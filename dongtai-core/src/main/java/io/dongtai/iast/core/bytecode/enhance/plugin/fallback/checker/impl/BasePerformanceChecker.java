package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.IPerformanceChecker;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;

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
    public PerformanceMetrics getMatchRiskThreshold(MetricsKey metrics, Properties cfg) {
        final List<PerformanceMetrics> performanceLimitRiskThreshold = RemoteConfigUtils.getPerformanceLimitRiskThreshold(cfg);
        for (PerformanceMetrics riskThreshold : performanceLimitRiskThreshold) {
            if (riskThreshold.getMetricsKey() == metrics) {
                return riskThreshold;
            }
        }
        return null;
    }

    @Override
    public PerformanceMetrics getMatchMaxThreshold(MetricsKey metrics, Properties cfg) {
        final List<PerformanceMetrics> performanceLimitRiskThreshold = RemoteConfigUtils.getPerformanceLimitMaxThreshold(cfg);
        for (PerformanceMetrics riskThreshold : performanceLimitRiskThreshold) {
            if (riskThreshold.getMetricsKey() == metrics) {
                return riskThreshold;
            }
        }
        return null;
    }
}
