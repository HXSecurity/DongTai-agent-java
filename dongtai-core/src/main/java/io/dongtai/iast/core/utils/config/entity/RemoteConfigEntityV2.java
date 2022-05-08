package io.dongtai.iast.core.utils.config.entity;

import lombok.Data;

import java.util.List;

@Data
public class RemoteConfigEntityV2 {

    /**
     * 是否允许自动降级
     */
    private Boolean enableAutoFallback;

    /**
     * 系统熔断后是否卸载
     */
    private Boolean systemIsUninstall;

    /**
     * JVM 熔断后是否卸载
     */
    private Boolean jvmIsUninstall;

    /**
     * 应用熔断后是否卸载
     */
    private Boolean applicationIsUninstall;

    /**
     * 性能熔断-不允许超过风险阈值的指标数量(0为不限制，达到阈值数时熔断)
     */
    private Integer performanceLimitRiskMaxMetricsCount;

    /**
     * 系统熔断阈值
     */
    private List<PerformanceEntity> system;

    /**
     * JVM 熔断阈值
     */
    private List<PerformanceEntity> jvm;

    /**
     * 应用熔断阈值
     */
    private List<PerformanceEntity> application;


}
