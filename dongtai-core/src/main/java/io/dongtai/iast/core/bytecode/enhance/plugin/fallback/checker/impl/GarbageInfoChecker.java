package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;

import java.util.List;
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
            final GarbageInfoMetrics.CollectionInfo tenuredThreshold = getCollectionInfoWithoutName(threshold, true);
            final GarbageInfoMetrics.CollectionInfo noTenuredThreshold = getCollectionInfoWithoutName(threshold, false);
            for (GarbageInfoMetrics.CollectionInfo each : now.getCollectionInfoList()) {
                GarbageInfoMetrics.CollectionInfo matchedThreshold = threshold.getMatchedFirst(each.getCollectionName());
                if (each.isTenured()) {
                    // 未找到对应名称的收集器阈值配置时，尝试寻找老年代的通用配置
                    if (matchedThreshold == null) {
                        if (tenuredThreshold != null) {
                            matchedThreshold = tenuredThreshold;
                        } else {
                            continue;
                        }
                    }
                    // 老年代比较gc次数和时间是否超过阈值
                    final boolean isGcCountOverThreshold = matchedThreshold.getCollectionCount() != null && each.getCollectionCount() != null
                            && each.getCollectionCount() > matchedThreshold.getCollectionCount();
                    final boolean isGcTimeOverThreshold = matchedThreshold.getCollectionTime() != null && each.getCollectionTime() != null
                            && each.getCollectionTime() > matchedThreshold.getCollectionTime();
                    return isGcCountOverThreshold || isGcTimeOverThreshold;
                } else {
                    // 未找到对应名称的收集器阈值配置时，尝试寻找年轻代的通用配置
                    if (matchedThreshold == null) {
                        if (noTenuredThreshold != null) {
                            matchedThreshold = noTenuredThreshold;
                        } else {
                            continue;
                        }
                    }
                    // 年轻代仅比较gc时间是否超过阈值
                    return matchedThreshold.getCollectionTime() != null && each.getCollectionTime() != null
                            && each.getCollectionTime() > matchedThreshold.getCollectionTime();
                }
            }
        }
        return false;
    }

    /**
     * 查找没有名称的收集器信息
     *
     * @param metrics 指标
     * @param tenured 是否是老年代
     * @return {@link GarbageInfoMetrics.CollectionInfo}
     */
    private GarbageInfoMetrics.CollectionInfo getCollectionInfoWithoutName(GarbageInfoMetrics metrics, Boolean tenured) {
        if (metrics != null) {
            final List<GarbageInfoMetrics.CollectionInfo> collectionInfoList = metrics.getCollectionInfoList();
            if (collectionInfoList != null && collectionInfoList.size() > 0) {
                if (tenured != null) {
                    for (GarbageInfoMetrics.CollectionInfo each : collectionInfoList) {
                        if (each.getCollectionName() == null) {
                            if (each.isTenured() == tenured) {
                                return each;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
