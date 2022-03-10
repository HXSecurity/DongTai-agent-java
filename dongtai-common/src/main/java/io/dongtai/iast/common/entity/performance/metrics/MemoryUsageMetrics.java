/*
MemoryUsage对象表示内存使用情况 的快照。MemoryUsage类的实例通常由用于获取有关 Java 虚拟机的单个内存池或整个 Java 虚拟机的堆或非堆内存的内存使用信息的方法构造。

一个MemoryUsage对象包含四个值：

init	表示 Java 虚拟机在启动期间从操作系统请求用于内存管理的初始内存量（以字节为单位）。Java 虚拟机可能会向操作系统请求额外的内存，也可能会随着时间的推移向系统释放内存。init的值可能未定义。
used	表示当前使用的内存量（以字节为单位）。
committed	表示保证可供 Java 虚拟机使用的内存量（以字节为单位）。提交的内存量可能会随时间而变化（增加或减少）。Java 虚拟机可能会向系统释放内存，并且提交的内存可能少于init。 commited将始终大于或等于used。
max	表示可用于内存管理的最大内存量（以字节为单位）。它的值可能是未定义的。如果已定义，最大内存量可能会随时间变化。如果定义了max ，则使用的内存量将始终小于或等于 max。它可能大于或小于commited。如果尝试增加已使用的内存，即使used <= max仍然为 true（例如，当系统的虚拟内存不足时），内存分配可能会失败。

下图显示了一个 max < commited的内存池示例。
          +----------------------------------------------+
          +////////////////           |                  +
          +////////////////           |                  +
          +----------------------------------------------+

          |--------|
             init
          |---------------|
                 used
          |---------------------------|
                     max
          |----------------------------------------------|
                                 committed
 */
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

    /**
     * 初始化内存
     */
    private Long init;
    /**
     * 已用内存
     */
    private Long used;
    /**
     * 已提交内存
     */
    private Long committed;
    /**
     * 最大内存(未限制时为-1)
     */
    private Long max;
    /**
     * 内存使用率百分比
     */
    private Double memUsagePercentage;

    public MemoryUsageMetrics() {
    }

    public MemoryUsageMetrics(Long init, Long used, Long committed, Long max) {
        this.init = init;
        this.used = used;
        this.committed = committed;
        this.max = max;
        if (used != null && used >= 0 && committed != null && committed >= 0) {
            this.memUsagePercentage = (1.0 - (used * 1.0 / committed)) * 100;
        }
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

    public Double getMemUsagePercentage() {
        return memUsagePercentage;
    }

    public void setMemUsagePercentage(Double memUsagePercentage) {
        this.memUsagePercentage = memUsagePercentage;
    }
}
