package io.dongtai.iast.agent.monitor.collector;

import io.dongtai.iast.agent.monitor.collector.impl.*;
import io.dongtai.iast.common.enums.MetricsKey;

import static io.dongtai.iast.common.enums.MetricsKey.*;

/**
 * 性能指标对应的收集器枚举
 *
 * @author chenyi
 * @date 2022/2/28
 */
public enum MetricsCollector {

    /**
     * JVM性能指标相关收集器
     */
    CPU_USAGE_COLLECTOR(CPU_USAGE, CpuUsageCollector.class, "cpu使用率收集器"),

    MEM_USAGE_COLLECTOR(MEM_USAGE, MemUsageCollector.class, "内存使用率收集器"),

    MEM_NO_HEAP_USAGE_COLLECTOR(MEM_NO_HEAP_USAGE, MemNoHeapUsageCollector.class, "堆外内存使用率收集器"),

    GARBAGE_INFO_COLLECTOR(GARBAGE_INFO, GarbageInfoCollector.class, "垃圾回收信息收集器"),

    ;


    /**
     * 指标key
     */
    private final MetricsKey key;

    /**
     * 收集器类型
     */
    private Class<? extends IPerformanceCollector> collector;

    /**
     * 描述
     */
    private final String desc;

    MetricsCollector(MetricsKey key, Class<? extends IPerformanceCollector> collector, String desc) {
        this.key = key;
        this.collector = collector;
        this.desc = desc;
    }

    public MetricsKey getKey() {
        return key;
    }

    public Class<? extends IPerformanceCollector> getCollector() {
        return collector;
    }

    public String getDesc() {
        return desc;
    }

    public static MetricsCollector getEnum(MetricsKey key) {
        if (key != null) {
            for (MetricsCollector each : MetricsCollector.values()) {
                if (key.equals(each.getKey())) {
                    return each;
                }
            }
        }
        return null;
    }
}
