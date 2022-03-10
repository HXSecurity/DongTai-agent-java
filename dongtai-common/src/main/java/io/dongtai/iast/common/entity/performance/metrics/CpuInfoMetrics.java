package io.dongtai.iast.common.entity.performance.metrics;

import java.io.Serializable;
/**
 * CPU使用率指标
 *
 * @author chenyi
 * @date 2022/3/1
 */
public class CpuInfoMetrics implements Serializable {
    private static final long serialVersionUID = 7786956147379364511L;

    /**
     * cpu使用百分比
     */
    private Double cpuUsagePercentage;

    public Double getCpuUsagePercentage() {
        return cpuUsagePercentage;
    }

    public void setCpuUsagePercentage(Double cpuUsagePercentage) {
        this.cpuUsagePercentage = cpuUsagePercentage;
    }

}
