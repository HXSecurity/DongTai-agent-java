package io.dongtai.iast.core.bytecode.enhance.plugin.fallback;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.SecondFallbackReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.SecondFallbackReportBody;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;
import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

/**
 * 降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class FallbackSwitch {

    private FallbackSwitch() {
        throw new IllegalStateException("Utility class");
    }

    // *************************************************************
    // 降级开关配置
    // *************************************************************

    /**
     * 高频hook点降级开关(线程隔离)
     */
    private static final BooleanThreadLocal HEAVY_HOOK_FALLBACK = new BooleanThreadLocal(false);

    /**
     * 高频流量降级开关
     */
    @Getter
    @Setter
    private static boolean HEAVY_TRAFFIC_LIMIT_FALLBACK = false;

    /**
     * 性能降级开关
     */
    @Getter
    @Setter
    private static boolean PERFORMANCE_FALLBACK = false;

    // *************************************************************
    // 二次降级配置
    // *************************************************************
    /**
     * 高频流量降级开启持续时间计时器
     */
    private static final StopWatch HEAVY_TRAFFIC_STOPWATCH = new StopWatch();
    /**
     * 性能降级开启持续时间计时器
     */
    private static final StopWatch PERFORMANCE_STOPWATCH = new StopWatch();


    /**
     * 是否对当前请求降级
     *
     * @return boolean 是否发生降级
     */
    public static boolean isRequestFallback() {
        return HEAVY_HOOK_FALLBACK.get() != null && HEAVY_HOOK_FALLBACK.get();
    }

    /**
     * 是否对引擎降级(全局增强点生效)
     *
     * @return boolean 是否发生降级
     */
    public static boolean isEngineFallback() {
        return HEAVY_TRAFFIC_LIMIT_FALLBACK || PERFORMANCE_FALLBACK;
    }



    public static void setHeavyHookFallback(boolean fallback) {
        HEAVY_HOOK_FALLBACK.set(fallback);
    }

    public static void clearHeavyHookFallback() {
        HEAVY_HOOK_FALLBACK.remove();
    }

    public static void setHeavyTrafficLimitFallback(boolean fallback) {
        HEAVY_TRAFFIC_LIMIT_FALLBACK = fallback;
        DongTaiLog.info("Engine heavyTraffic fallback is {}, Engine {} successfully", fallback ? "open" : "close", fallback ? "shut down" : "opened");
        limitSwitchFrequency(fallback);
        calculateFallbackDuration(HEAVY_TRAFFIC_STOPWATCH, fallback, SecondFallbackReasonEnum.TRAFFIC_FALLBACK_DURATION);
    }

    public static void setPerformanceFallback(boolean fallback) {
        PERFORMANCE_FALLBACK = fallback;
        DongTaiLog.info("Engine performance fallback is {}, Engine {} successfully", fallback ? "open" : "close", fallback ? "shut down" : "opened");
        limitSwitchFrequency(fallback);
        calculateFallbackDuration(PERFORMANCE_STOPWATCH, fallback, SecondFallbackReasonEnum.PERFORMANCE_FALLBACK_DURATION);
    }

    /**
     * 限制开关切换速率
     */
    private static void limitSwitchFrequency(boolean fallback) {
        if (fallback && !EngineManager.getFallbackManager().getFallbackSwitchFrequencyLimiter().acquire()) {
            DongTaiLog.info("Switch frequency is over threshold.");
            SecondFallbackReport.appendLog(new SecondFallbackReportBody.FrequencyOverThresholdLog(SecondFallbackReasonEnum.FALLBACK_SWITCH_FREQUENCY));
        }
    }

    /**
     * 计算降级持续时间
     */
    private static void calculateFallbackDuration(StopWatch stopWatch, boolean fallback, SecondFallbackReasonEnum secondFallbackType) {
        if (fallback) {
            // 打开开关时，计时器开始计时
            if (stopWatch.isStopped()) {
                stopWatch.start();
            }
        } else {
            if (!stopWatch.isStarted()) {
                return;
            }
            // 关闭开关时，计时器停止
            stopWatch.stop();
            // 计算持续时间超限则记录下来
            final long switchOpenStatusDurationThreshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null);
            if (stopWatch.getTime() >= switchOpenStatusDurationThreshold) {
                SecondFallbackReport.appendLog(new SecondFallbackReportBody.DurationOverThresholdLog(
                        secondFallbackType, stopWatch, switchOpenStatusDurationThreshold));
            }
            stopWatch.reset();
        }
    }

    /**
     * 判断是否需要二次降级
     */
    protected static boolean isNeedSecondFallback() {
        // 1、判断此时流量熔断器开关是否处于打开状态并达到阈值
        long switchOpenStatusDurationThreshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null);
        if (HEAVY_TRAFFIC_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
            SecondFallbackReport.appendLog(new SecondFallbackReportBody.DurationOverThresholdLog(
                    SecondFallbackReasonEnum.TRAFFIC_FALLBACK_DURATION, HEAVY_TRAFFIC_STOPWATCH, switchOpenStatusDurationThreshold));
        }
        // 2、判断此时性能熔断器开关是否处于打开状态并达到阈值
        if (PERFORMANCE_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
            SecondFallbackReport.appendLog(new SecondFallbackReportBody.DurationOverThresholdLog(
                    SecondFallbackReasonEnum.PERFORMANCE_FALLBACK_DURATION, PERFORMANCE_STOPWATCH, switchOpenStatusDurationThreshold));
        }
        // 3、当二次降级报告日志不为空时，需要进行二次降级
        final boolean isNeedSecondFallback = !SecondFallbackReport.isSecondFallbackLogEmpty();
        if (isNeedSecondFallback) {
            SecondFallbackReport.sendReport();
        }
        return isNeedSecondFallback;
    }

    /**
     * 二次降级原因
     *
     * @author liyuan
     * @date 2022/03/10
     */
    public enum SecondFallbackReasonEnum {
        /**
         * 二次降级原因
         */
        PERFORMANCE_FALLBACK_DURATION("performanceFallbackDuration", "性能降级时长超限"),
        TRAFFIC_FALLBACK_DURATION("trafficFallbackDuration", "流量降级时长超限"),
        FALLBACK_SWITCH_FREQUENCY("fallbackSwitchFrequency", "降级开关触发频率超限"),
        ;

        private final String fallbackType;

        private final String desc;

        public String getFallbackType() {
            return fallbackType;
        }

        public String getDesc() {
            return desc;
        }

        SecondFallbackReasonEnum(String fallbackType, String desc) {
            this.fallbackType = fallbackType;
            this.desc = desc;
        }
    }
}
