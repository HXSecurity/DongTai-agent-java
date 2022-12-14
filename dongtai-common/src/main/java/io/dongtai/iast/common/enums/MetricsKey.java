package io.dongtai.iast.common.enums;

import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;


/**
 * 性能指标枚举
 *
 * @author chenyi
 * @date 2022/2/28
 */
public enum MetricsKey {

    /**
     * JVM相关性能指标枚举
     */
    CPU_USAGE("cpuUsage", CpuInfoMetrics.class, "cpu使用率"),

    MEM_USAGE("memoryUsage", MemoryUsageMetrics.class, "内存使用率"),
    ;


    /**
     * key
     */
    private final String key;

    /**
     * 指标值类型
     */
    private final Class<?> valueType;

    /**
     * 描述
     */
    private final String desc;

    MetricsKey(String key, Class<?> valueType, String desc) {
        this.key = key;
        this.valueType = valueType;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public String getDesc() {
        return desc;
    }

    public MetricsKey getEnum(String key) {
        if (key != null) {
            for (MetricsKey each : MetricsKey.values()) {
                if (key.equals(each.getKey())) {
                    return each;
                }
            }
        }
        return null;
    }
}
