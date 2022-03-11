package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;

import java.util.Properties;


/**
 * 线程信息检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class ThreadInfoChecker extends BasePerformanceChecker {

    @Override
    public boolean isPerformanceRisk(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchRiskThreshold(nowMetrics.getMetricsKey(), cfg);
        return checkThreadInfoOverThreshold(nowMetrics, thresholdMetrics);
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(nowMetrics.getMetricsKey(), cfg);
        return checkThreadInfoOverThreshold(nowMetrics, thresholdMetrics);
    }

    private boolean checkThreadInfoOverThreshold(PerformanceMetrics nowMetrics, PerformanceMetrics thresholdMetrics) {
        if (thresholdMetrics != null) {
            final ThreadInfoMetrics threshold = thresholdMetrics.getMetricsValue(ThreadInfoMetrics.class);
            final ThreadInfoMetrics now = nowMetrics.getMetricsValue(ThreadInfoMetrics.class);
            // 当前线程数
            if (threshold.getThreadCount() != null && now.getThreadCount() >= threshold.getThreadCount()) {
                return true;
            }
            // 比较洞态守护线程数
            return threshold.getDongTaiThreadCount() != null && now.getDongTaiThreadCount() >= threshold.getDongTaiThreadCount();
        }
        return false;
    }
}
