package io.dongtai.iast.common.entity.performance.metrics;

import java.io.Serializable;
import java.lang.management.MemoryUsage;

/**
 * 内存使用率指标
 *
 * @author chenyi
 * @date 2022/3/1
 * @see java.lang.management.MemoryUsage
 */
public class MemoryUsageMetrics implements Serializable {
    private static final long serialVersionUID = -809690992297671496L;

    private Long init;
    private Long used;
    private Long committed;
    private Long max;

    public MemoryUsageMetrics(Long init, Long used, Long committed, Long max) {
        this.init = init;
        this.used = used;
        this.committed = committed;
        this.max = max;
    }

    public static MemoryUsageMetrics clone(MemoryUsage memoryUsage) {
        return new MemoryUsageMetrics(memoryUsage.getInit(), memoryUsage.getUsed(), memoryUsage.getCommitted(), memoryUsage.getMax());
    }

    public Long getInit() {
        return init;
    }

    public void setInit(Long init) {
        this.init = init;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public Long getCommitted() {
        return committed;
    }

    public void setCommitted(Long committed) {
        this.committed = committed;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "init = " + this.init + "(" + (this.init >> 10) + "K) " +
                "used = " + this.used + "(" + (this.used >> 10) + "K) " +
                "committed = " + this.committed + "(" + (this.committed >> 10) + "K) " +
                "max = " + this.max + "(" + (this.max >> 10) + "K)";
    }

}
