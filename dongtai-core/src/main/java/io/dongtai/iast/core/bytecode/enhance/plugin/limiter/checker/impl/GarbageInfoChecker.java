package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;

import java.util.Properties;


/**
 * 垃圾回收信息检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class GarbageInfoChecker extends BasePerformanceChecker {

    @Override
    public boolean isPerformanceRisk(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchRiskThreshold(nowMetrics.getMetricsKey(), cfg);
        return checkIsGarbageInfoOverThreshold(nowMetrics, thresholdMetrics);
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(nowMetrics.getMetricsKey(), cfg);
        return checkIsGarbageInfoOverThreshold(nowMetrics, thresholdMetrics);
    }

    private boolean checkIsGarbageInfoOverThreshold(PerformanceMetrics nowMetrics, PerformanceMetrics thresholdMetrics) {
        if (thresholdMetrics != null) {
            final GarbageInfoMetrics threshold = thresholdMetrics.getMetricsValue(GarbageInfoMetrics.class);
            final GarbageInfoMetrics now = nowMetrics.getMetricsValue(GarbageInfoMetrics.class);
            for (GarbageInfoMetrics.CollectionInfo each : now.getCollectionInfoList()) {
                GarbageInfoMetrics.CollectionInfo matchedThreshold = threshold.getMatchedCollectionInfo(each.getCollectionName());
                // 未找到对应名称的收集器阈值配置时，尝试寻找不区分收集器名称的通用配置
                if (matchedThreshold == null) {
                    matchedThreshold = threshold.getMatchedCollectionInfo(null);
                    if (matchedThreshold == null) {
                        continue;
                    }
                }
                if (each.isTenured()) {
                    // 老年代比较gc次数和时间是否超过阈值
                    final boolean isGcCountOverThreshold = matchedThreshold.getCollectionCount() != null && each.getCollectionCount() != null
                            && each.getCollectionCount() > matchedThreshold.getCollectionCount();
                    final boolean isGcTimeOverThreshold = matchedThreshold.getCollectionTime() != null && each.getCollectionTime() != null
                            && each.getCollectionTime() > matchedThreshold.getCollectionTime();
                    return isGcCountOverThreshold || isGcTimeOverThreshold;
                } else {
                    // 年轻代仅比较gc时间是否超过阈值
                    return matchedThreshold.getCollectionTime() != null && each.getCollectionTime() != null
                            && each.getCollectionTime() > matchedThreshold.getCollectionTime();
                }
            }
        }
        return false;
    }
}
