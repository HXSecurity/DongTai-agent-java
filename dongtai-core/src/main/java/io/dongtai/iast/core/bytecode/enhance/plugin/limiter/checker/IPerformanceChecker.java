package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;

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
     * @param metrics 性能指标
     * @param cfg                配置
     * @return boolean
     */
    boolean isPerformanceRisk(PerformanceMetrics metrics, Properties cfg);

    /**
     * 性能是否达到限制值
     *
     * @param metrics 性能指标
     * @param cfg                配置
     * @return boolean
     */
    boolean isPerformanceOverLimit(PerformanceMetrics metrics, Properties cfg);
}
