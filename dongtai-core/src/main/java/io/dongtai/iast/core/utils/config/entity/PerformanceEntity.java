package io.dongtai.iast.core.utils.config.entity;

import lombok.Data;

@Data
public class PerformanceEntity {

    /**
     * 熔断指标名称
     */
    private String fallbackName;

    /**
     * 熔断条件
     */
    private String conditions;

    /**
     * 熔断值
     */
    private Double value;

}
