package io.dongtai.iast.agent.monitor.collector;

import io.dongtai.iast.agent.monitor.collector.impl.*;
import io.dongtai.iast.common.enums.MetricsKey;

import static io.dongtai.iast.common.enums.MetricsKey.*;

/**
 * 性能指标绑定的性能收集器枚举
 *
 * @author chenyi
 * @date 2022/2/28
 */
public enum MetricsBindCollectorEnum {

    /**
     * JVM性能指标相关收集器
     */
    CPU_USAGE_COLLECTOR(CPU_USAGE, CpuUsageCollector.class, "绑定cpu使用率收集器"),

    MEM_USAGE_COLLECTOR(MEM_USAGE, MemUsageCollector.class, "绑定内存使用率收集器"),

    MEM_NO_HEAP_USAGE_COLLECTOR(MEM_NO_HEAP_USAGE, MemNoHeapUsageCollector.class, "绑定堆外内存使用率收集器"),

    GARBAGE_INFO_COLLECTOR(GARBAGE_INFO, GarbageInfoCollector.class, "绑定垃圾回收信息收集器"),

    THREAD_INFO_COLLECTOR(THREAD_INFO, ThreadInfoCollector.class, "绑定线程信息收集器"),

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

    MetricsBindCollectorEnum(MetricsKey key, Class<? extends IPerformanceCollector> collector, String desc) {
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

    public static MetricsBindCollectorEnum getEnum(MetricsKey key) {
        if (key != null) {
            for (MetricsBindCollectorEnum each : MetricsBindCollectorEnum.values()) {
                if (key.equals(each.getKey())) {
                    return each;
                }
            }
        }
        return null;
    }
}
