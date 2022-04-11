package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;

import java.util.Properties;

/**
 * 性能检测器接口
 *
 * @author chenyi
 * @date 2022/3/3
 */
public interface IPerformanceChecker {

    /**
     * 性能是否达到风险值
     *
     * @param nowMetrics 性能指标
     * @param cfg        配置
     * @return boolean
     */
    boolean isPerformanceRisk(PerformanceMetrics nowMetrics, Properties cfg);

    /**
     * 性能是否达到限制值
     *
     * @param nowMetrics 性能指标
     * @param cfg        配置
     * @return boolean
     */
    boolean isPerformanceOverLimit(PerformanceMetrics nowMetrics, Properties cfg);

    /**
     * 获取匹配的风险阈值
     *
     * @param metrics 指标
     * @param cfg     配置
     * @return {@link PerformanceMetrics}
     */
    PerformanceMetrics getMatchRiskThreshold(MetricsKey metrics, Properties cfg);

    /**
     * 获取匹配的最大阈值
     *
     * @param metrics 指标
     * @param cfg     配置
     * @return {@link PerformanceMetrics}
     */
    PerformanceMetrics getMatchMaxThreshold(MetricsKey metrics, Properties cfg);
}
