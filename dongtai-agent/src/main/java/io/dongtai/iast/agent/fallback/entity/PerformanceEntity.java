package io.dongtai.iast.agent.fallback.entity;

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

    public String getFallbackName() {
        return fallbackName;
    }

    public void setFallbackName(String fallbackName) {
        this.fallbackName = fallbackName;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
