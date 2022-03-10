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
    // 高频流量限流相关配置
    // *************************************************************

    /**
     * 高频流量限流-每秒获得令牌数
     */
    private Double heavyTrafficLimitTokenPerSecond;
    /**
     * 高频流量限流-初始预放置令牌时间
     */
    private Double heavyTrafficLimitInitBurstSeconds;
    /**
     * 高频流量限流-断路状态等待时间(不能大于等于secondFallbackDuration)
     */
    private Integer heavyTrafficBreakerWaitDuration;

    // *************************************************************
    // 性能熔断阈值相关配置
    // *************************************************************

    /**
     * 性能熔断-统计窗口大小
     */
    private Integer performanceBreakerWindowSize;
    /**
     * 性能熔断-失败率阈值
     */
    private Double performanceBreakerFailureRate;
    /**
     * 性能熔断-自动转半开的等待时间(单位:秒)
     */
    private Integer performanceBreakerWaitDuration;
    /**
     * 性能熔断-不允许超过风险阈值的指标数量(0为不限制，达到阈值数时熔断)
     */
    private Integer performanceLimitRiskMaxMetricsCount;
    /**
     * 性能熔断-风险阈值配置
     */
    private PerformanceLimitThreshold performanceLimitRiskThreshold;
    /**
     * 性能熔断-最大阈值配置
     */
    private PerformanceLimitThreshold performanceLimitMaxThreshold;

    // *************************************************************
    // 二次降级操作限流相关配置
    // *************************************************************

    /**
     * 二次降级-降级开关打开频率限制-每秒获得令牌数
     */
    private Double secondFallbackFrequencyTokenPerSecond;
    /**
     * 二次降级-降级开关打开频率限制-初始预放置令牌时间
     */
    private Double secondFallbackFrequencyInitBurstSeconds;
    /**
     * 二次降级-降级开关持续时间限制-降级开关打开状态持续最大时间(ms)
     */
    private Long secondFallbackDuration;

}
