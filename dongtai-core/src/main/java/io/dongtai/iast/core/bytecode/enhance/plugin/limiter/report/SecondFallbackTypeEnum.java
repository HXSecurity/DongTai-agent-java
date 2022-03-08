package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report;

/**
 * 二次降级开关
 *
 * @author liyuan40
 * @date 2022/3/8 17:11
 */
public enum SecondFallbackTypeEnum {
    /**
     * 二次降级的类型
     */
    PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_TIME("performanceSwitcher", "性能降级打开状态持续时间超过阈值"),
    TRAFFIC_SWITCHER_OPEN_DURATION_OVER_TIME("heavyTrafficSwitcher", "流量降级打开状态持续时间超过阈值"),
    SWITCH_OPEN_FREQUENCY_OVER_RATE("switchOpenRate", "开关打开频率超过限速"),
    ;

    private final String fallbackType;

    private final String desc;

    public String getFallbackType() {
        return fallbackType;
    }

    public String getDesc() {
        return desc;
    }

    SecondFallbackTypeEnum(String fallbackType, String desc) {
        this.fallbackType = fallbackType;
        this.desc = desc;
    }
}
