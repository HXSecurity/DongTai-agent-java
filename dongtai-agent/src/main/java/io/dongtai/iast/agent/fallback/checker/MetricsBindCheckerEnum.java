package io.dongtai.iast.agent.fallback.checker;

import io.dongtai.iast.agent.fallback.checker.impl.CpuUsageChecker;
import io.dongtai.iast.agent.fallback.checker.impl.MemUsageChecker;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import static io.dongtai.iast.common.enums.MetricsKey.*;

/**
 * 性能指标绑定的性能检查器枚举
 *
 * @author chenyi
 * @date 2022/3/4
 */
public enum MetricsBindCheckerEnum {

    /**
     * JVM性能指标相关检查器
     */
    CPU_USAGE_CHECKER(CPU_USAGE, CpuUsageChecker.class, "绑定cpu使用率检查器"),

    MEM_USAGE_CHECKER(MEM_USAGE, MemUsageChecker.class, "绑定内存使用率检查器"),
    ;


    /**
     * 指标key
     */
    private final MetricsKey key;

    /**
     * 检查器类型
     */
    private final Class<? extends IPerformanceChecker> checker;

    /**
     * 描述
     */
    private final String desc;

    MetricsBindCheckerEnum(MetricsKey key, Class<? extends IPerformanceChecker> checker, String desc) {
        this.key = key;
        this.checker = checker;
        this.desc = desc;
    }

    /**
     * 获取枚举对应的检查器实例
     */
    public static IPerformanceChecker newCheckerInstance(MetricsKey key) {
        final MetricsBindCheckerEnum anEnum = getEnum(key);
        if (anEnum == null) {
            return null;
        }
        try {
            return anEnum.getChecker().newInstance();
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.AGENT_FALLBACK_CHECKER_CREATE_FAILED, e.getMessage());
            return null;
        }
    }

    public MetricsKey getKey() {
        return key;
    }

    public Class<? extends IPerformanceChecker> getChecker() {
        return checker;
    }

    public String getDesc() {
        return desc;
    }

    public static MetricsBindCheckerEnum getEnum(MetricsKey key) {
        if (key != null) {
            for (MetricsBindCheckerEnum each : MetricsBindCheckerEnum.values()) {
                if (key.equals(each.getKey())) {
                    return each;
                }
            }
        }
        return null;
    }
}
