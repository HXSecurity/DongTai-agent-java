package io.dongtai.iast.core.utils.config.entity;

import io.dongtai.iast.common.entity.performance.metrics.*;
import lombok.Data;

/**
 * 性能限制阈值配置
 *
 * @author chenyi
 * @date 2022/03/10
 */
@Data
public class PerformanceLimitThreshold {

    /**
     * cpu使用率阈值配置
     */
    private CpuInfoMetrics cpuUsage;
    /**
     * 内存使用率阈值配置
     */
    private MemoryUsageMetrics memoryUsage;
    /**
     * 堆外内存使用率阈值配置
     */
    private MemoryUsageMetrics memoryNoHeapUsage;
    /**
     * 垃圾回收信息阈值配置
     */
    private GarbageInfoMetrics garbageInfo;
    /**
     * 线程信息阈值配置
     */
    private ThreadInfoMetrics threadInfo;

}
