package io.dongtai.iast.core.utils.config.entity;

import lombok.Data;

/**
 * 远程配置实体
 *
 * @author me
 * @date 2022/03/10
 */
@Data
public class RemoteConfigEntity {

    // *************************************************************
    // 全局配置
    // *************************************************************

    /**
     * 是否允许自动降级
     */
    private Boolean enableAutoFallback;

    // *************************************************************
    // 高频hook限流相关配置
    // *************************************************************
    /**
     * 高频hook限流-每秒获得令牌数
     */
    private Double hookLimitTokenPerSecond;
    /**
     * 高频hook限流-初始预放置令牌时间
     */
    private Double hookLimitInitBurstSeconds;

    // *************************************************************
    // 性能熔断阈值相关配置
    // *************************************************************

    /**
     * 性能断路器-统计窗口大小
     */
    private Integer performanceBreakerWindowSize;
    /**
     * 性能断路器-失败率阈值
     */
    private Double performanceBreakerFailureRate;
    /**
     * 性能断路器-自动转半开的等待时间(单位:秒)
     */
    private Integer performanceBreakerWaitDuration;
    /**
     * 性能断路器-不允许超过风险阈值的指标数量(0为不限制，达到阈值数时熔断)
     */
    private Integer maxRiskMetricsCount;
    /**
     * 性能断路器-风险阈值配置
     */
    private PerformanceLimitThreshold performanceLimitRiskThreshold;
    /**
     * 性能断路器-最大阈值配置
     */
    private PerformanceLimitThreshold performanceLimitMaxThreshold;


}
