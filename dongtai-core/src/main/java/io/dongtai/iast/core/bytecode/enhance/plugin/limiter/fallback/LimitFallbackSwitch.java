package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback;

import io.dongtai.iast.common.utils.FixSizeLinkedList;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.SecondFallbackReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.SecondFallbackReportBody;
import io.dongtai.iast.core.utils.RemoteConfigUtils;
import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.lang3.time.StopWatch;

import java.text.SimpleDateFormat;

/**
 * 限制降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class LimitFallbackSwitch {

    private LimitFallbackSwitch() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 高频hook点降级开关(线程隔离)
     */
    private static final BooleanThreadLocal HEAVY_HOOK_FALLBACK = new BooleanThreadLocal(false);
    /**
     * 高频流量降级开关
     */
    private static boolean HEAVY_TRAFFIC_LIMIT_FALLBACK = false;
    /**
     * 性能降级开关
     */
    private static boolean PERFORMANCE_FALLBACK = false;
    /**
     * 高频流量降级打开状态计时器
     */
    private static final StopWatch HEAVY_TRAFFIC_STOPWATCH = new StopWatch();
    /**
     * 性能降级打开状态计时器
     */
    private static final StopWatch PERFORMANCE_STOPWATCH = new StopWatch();

    /**
     * 记录会触发二次降级的日志列表
     */
    private static final FixSizeLinkedList<SecondFallbackReportBody.AbstractSecondFallbackReportLog> FALLBACK_REPORT_LOGS = new FixSizeLinkedList<>(30);

    /**
     * 请求对当前请求降级
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


    public static boolean getHeavyTrafficLimitFallback() {
        return HEAVY_TRAFFIC_LIMIT_FALLBACK;
    }

    public boolean getPerformanceFallback() {
        return PERFORMANCE_FALLBACK;
    }

    public static void setHeavyHookFallback(boolean fallback) {
        HEAVY_HOOK_FALLBACK.set(fallback);
    }

    public static void clearHeavyHookFallback() {
        HEAVY_HOOK_FALLBACK.remove();
    }

    public static void setHeavyTrafficLimitFallback(boolean fallback) {
        HEAVY_TRAFFIC_LIMIT_FALLBACK = fallback;
        logEngineStatus(fallback, "heavyTraffic");
        acquireFromTokenBucket(fallback);
        handleStopWatch(HEAVY_TRAFFIC_STOPWATCH, fallback, SecondFallbackTypeEnum.HEAVY_TRAFFIC_SWITCHER_OPEN_DURATION_OVER_THRESHOLD);
    }

    public static void setPerformanceFallback(boolean fallback) {
        PERFORMANCE_FALLBACK = fallback;
        logEngineStatus(fallback, "performance");
        acquireFromTokenBucket(fallback);
        handleStopWatch(PERFORMANCE_STOPWATCH, fallback, SecondFallbackTypeEnum.PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_THRESHOLD);
    }

    /**
     * 打印引擎状态变化日志
     *
     * @param fallback 降级状态
     */
    public static void logEngineStatus(boolean fallback, String switcher) {
        String switchStatus = fallback ? "open" : "close";
        String engineOperation = fallback ? "shut down" : "opened";
        DongTaiLog.info("Engine {} fallback is {}, Engine {} successfully", switcher, switchStatus, engineOperation);
    }

    /**
     * 打开开关时获取令牌，获取不到令牌则记录下来
     */
    private static void acquireFromTokenBucket(boolean fallback) {
        if (fallback && !EngineManager.getLimiterManager().getSwitchRateLimiter().acquire()) {
            DongTaiLog.info("Frequency of SecondFallbackSwitch transformation excesses threshold.");
            String occurTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
            SecondFallbackReportBody.SwitchFrequencyOverThresholdLog body = new SecondFallbackReportBody.SwitchFrequencyOverThresholdLog(SecondFallbackTypeEnum.SWITCH_FALLBACK_FREQUENCY_OVER_RATE.getFallbackType(), occurTime);
            FALLBACK_REPORT_LOGS.add(body);
        }
    }

    /**
     * 处理计时器
     *
     * @param stopWatch          计时器
     * @param secondFallbackType 二次降级类型
     */
    private static void handleStopWatch(StopWatch stopWatch, boolean fallback, SecondFallbackTypeEnum secondFallbackType) {
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
            if (stopWatch.getTime() >= RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null)) {
                FALLBACK_REPORT_LOGS.add(new SecondFallbackReportBody.SwitchOpenTimeOverThresholdReportLog(secondFallbackType, stopWatch));
            }
            stopWatch.reset();
        }
    }

    /**
     * 判断是否需要二次降级
     */
    public static boolean isNeedSecondFallback() {
        // 1、判断此时流量熔断器开关是否处于打开状态并达到阈值
        long switchOpenStatusDurationThreshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(null);
        if (HEAVY_TRAFFIC_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
            FALLBACK_REPORT_LOGS.add(new SecondFallbackReportBody.SwitchOpenTimeOverThresholdReportLog(SecondFallbackTypeEnum.HEAVY_TRAFFIC_SWITCHER_OPEN_DURATION_OVER_THRESHOLD, HEAVY_TRAFFIC_STOPWATCH));
        }
        // 2、判断此时性能熔断器开关是否处于打开状态并达到阈值
        if (PERFORMANCE_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
            FALLBACK_REPORT_LOGS.add(new SecondFallbackReportBody.SwitchOpenTimeOverThresholdReportLog(SecondFallbackTypeEnum.PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_THRESHOLD, PERFORMANCE_STOPWATCH));
        }
        // 3、判断当前检查周期内是否存在过需要二次降级的情况
        if (!FALLBACK_REPORT_LOGS.isEmpty()) {
            SecondFallbackReport.sendReport(FALLBACK_REPORT_LOGS);
            FALLBACK_REPORT_LOGS.clear();
            return true;
        }

        return false;
    }

    /**
     * 二次降级类型
     *
     * @author liyuan
     * @date 2022/03/10
     */
    public enum SecondFallbackTypeEnum {
        /**
         * 二次降级的类型
         */
        PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_THRESHOLD("performanceSwitcherOpenDurationOverThreshold", "性能降级打开状态持续时间超过阈值"),
        HEAVY_TRAFFIC_SWITCHER_OPEN_DURATION_OVER_THRESHOLD("heavyTrafficSwitcherOpenDurationOverThreshold", "高频流量降级打开状态持续时间超过阈值"),
        SWITCH_FALLBACK_FREQUENCY_OVER_RATE("switchFallbackFrequencyOverRate", "熔断器熔断频率超过限速"),
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
}
