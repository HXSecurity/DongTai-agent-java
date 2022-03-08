package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.SecondFallbackReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.SecondFallbackTypeEnum;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.AbstractFallbackReportBody;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.PerformanceBreakReportBody;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.SwitchFrequencyOverThresholdFallbackReportBody;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.body.SwitchOpenTimeOverThresholdFallbackReportBody;
import io.dongtai.iast.core.utils.RemoteConfigUtils;
import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * 限制降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class LimitFallbackSwitch {

    /**
     * 高频hook点降级开关(线程隔离)
     */
    private static final BooleanThreadLocal HEAVY_HOOK_FALLBACK = new BooleanThreadLocal(false);
    /**
     * 大流量降级开关
     */
    private static boolean HEAVY_TRAFFIC_LIMIT_FALLBACK = false;
    /**
     * 性能降级开关
     */
    private static boolean PERFORMANCE_FALLBACK = false;
    /**
     * 异常降级开关
     */
    private static boolean EXCEPTION_FALLBACK = false;
    /**
     * 大流量降级打开状态计时器
     */
    private static final StopWatch HEAVY_TRAFFIC_STOPWATCH = new StopWatch();
    /**
     * 性能降级打开状态计时器
     */
    private static final StopWatch PERFORMANCE_STOPWATCH = new StopWatch();
    /**
     * 开关开启状态持续最大时间(ms)
     */
    private static long switchOpenStatusDurationThreshold;

    public LimitFallbackSwitch(Properties cfg) {
        switchOpenStatusDurationThreshold = RemoteConfigUtils.getSwitchOpenStatusDurationThreshold(cfg);
    }

    /**
     * 记录开关打开持续时间
     */
    private static PerformanceBreakReportBody.FixSizeLinkedList<AbstractFallbackReportBody> switchOpenDurationTimeRecord = new PerformanceBreakReportBody.FixSizeLinkedList<>(30);

    /**
     * 记录操作开关获取不到令牌
     */
    private static PerformanceBreakReportBody.FixSizeLinkedList<AbstractFallbackReportBody> switchOpenTokenNotAcquiredRecord = new PerformanceBreakReportBody.FixSizeLinkedList<>(30);

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
        return HEAVY_TRAFFIC_LIMIT_FALLBACK || PERFORMANCE_FALLBACK || EXCEPTION_FALLBACK;
    }


    public static boolean getHeavyTrafficLimitFallback() {
        return HEAVY_TRAFFIC_LIMIT_FALLBACK;
    }

    public boolean getPerformanceFallback() {
        return PERFORMANCE_FALLBACK;
    }

    public static boolean getExceptionFallback() {
        return EXCEPTION_FALLBACK;
    }

    public static void setHeavyHookFallback(boolean fallback) {
        HEAVY_HOOK_FALLBACK.set(fallback);
    }

    public static void clearHeavyHookFallback() {
        HEAVY_HOOK_FALLBACK.remove();
    }

    public static void setHeavyTrafficLimitFallback(boolean fallback) {
        DongTaiLog.warn("transform heavy traffic switch:{} -> {}", HEAVY_TRAFFIC_LIMIT_FALLBACK, fallback);
        HEAVY_TRAFFIC_LIMIT_FALLBACK = fallback;
        if (fallback) {
            if (!EngineManager.getLimiterManager().getSwitchRateLimiter().acquire()) {
                handleNotAcquiredToken();
            }
            if (HEAVY_TRAFFIC_STOPWATCH.isStopped()) {
                HEAVY_TRAFFIC_STOPWATCH.reset();
                HEAVY_TRAFFIC_STOPWATCH.start();
            }
        }

        if (!fallback && HEAVY_TRAFFIC_STOPWATCH.isStarted()) {
            HEAVY_TRAFFIC_STOPWATCH.stop();
            if (HEAVY_TRAFFIC_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
                String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(HEAVY_TRAFFIC_STOPWATCH.getStartTime());
                switchOpenDurationTimeRecord.add(new SwitchOpenTimeOverThresholdFallbackReportBody(SecondFallbackTypeEnum.TRAFFIC_SWITCHER_OPEN_DURATION_OVER_TIME.getFallbackType(), startTime, HEAVY_TRAFFIC_STOPWATCH.getTime(), switchOpenStatusDurationThreshold));
            }
            HEAVY_TRAFFIC_STOPWATCH.reset();
        }
    }

    public static void setPerformanceFallback(boolean fallback) {
        PERFORMANCE_FALLBACK = fallback;
        if (fallback) {
            DongTaiLog.info("Engine performance fallback is open, Engine shut down successfully");
            if (!EngineManager.getLimiterManager().getSwitchRateLimiter().acquire()) {
                handleNotAcquiredToken();
            }
            if (PERFORMANCE_STOPWATCH.isStopped()) {
                PERFORMANCE_STOPWATCH.reset();
                PERFORMANCE_STOPWATCH.start();
            }
        } else {
            DongTaiLog.info("Engine performance fallback is close, Engine opened successfully");
            if (PERFORMANCE_STOPWATCH.isStarted()) {
                PERFORMANCE_STOPWATCH.stop();
                if (PERFORMANCE_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
                    String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(PERFORMANCE_STOPWATCH.getStartTime());
                    switchOpenDurationTimeRecord.add(new SwitchOpenTimeOverThresholdFallbackReportBody(SecondFallbackTypeEnum.PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_TIME.getFallbackType(), startTime, PERFORMANCE_STOPWATCH.getTime(), switchOpenStatusDurationThreshold));
                }
                PERFORMANCE_STOPWATCH.reset();
            }
        }
    }

    /**
     * 开关操作获取不到令牌时的操作
     */
    private static void handleNotAcquiredToken() {
        DongTaiLog.warn("Second fallback switch operation frequency excess threshold.");
        String occurTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        SwitchFrequencyOverThresholdFallbackReportBody body = new SwitchFrequencyOverThresholdFallbackReportBody(SecondFallbackTypeEnum.SWITCH_OPEN_FREQUENCY_OVER_RATE.getFallbackType(), occurTime);
        switchOpenTokenNotAcquiredRecord.add(body);
    }

    /**
     * 发送报告
     */
    private static void sendReport(JSONObject report) {
        SecondFallbackReport.sendSecondFallbackReport(report);
    }

    /**
     * 判断是否需要卸载引擎
     */
    public static boolean isNeedTurnOffEngine() {
        try {
            boolean result = false;
            JSONObject report = new JSONObject();
            // 1、判断检查周期内是否消耗完过开关次数令牌
            if (!switchOpenTokenNotAcquiredRecord.isEmpty()) {
                // 增加报告内容
                report.put("notAcquiredSwitchOpenToken", switchOpenTokenNotAcquiredRecord);
                result = true;
            }

            // 2、判断当前检查周期内流量熔断器开关是否持续打开并达到阈值
            if (HEAVY_TRAFFIC_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
                String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(HEAVY_TRAFFIC_STOPWATCH.getStartTime());
                switchOpenDurationTimeRecord.add(new SwitchOpenTimeOverThresholdFallbackReportBody(SecondFallbackTypeEnum.TRAFFIC_SWITCHER_OPEN_DURATION_OVER_TIME.getFallbackType(), startTime, HEAVY_TRAFFIC_STOPWATCH.getTime(), switchOpenStatusDurationThreshold));
                result = true;
            }
            // 3、判断当前检查周期内性能熔断器开关是否持续打开并达到阈值
            if (PERFORMANCE_STOPWATCH.getTime() >= switchOpenStatusDurationThreshold) {
                String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(PERFORMANCE_STOPWATCH.getStartTime());
                switchOpenDurationTimeRecord.add(new SwitchOpenTimeOverThresholdFallbackReportBody(SecondFallbackTypeEnum.PERFORMANCE_SWITCHER_OPEN_DURATION_OVER_TIME.getFallbackType(), startTime, PERFORMANCE_STOPWATCH.getTime(), switchOpenStatusDurationThreshold));
                result = true;
            }
            // 4、判断当前检查周期内是否存在过开关开启时间达到阈值
            if (!switchOpenDurationTimeRecord.isEmpty()) {
                // 增加报告内容
                report.put("switchOpenDurationOverThreshold", switchOpenDurationTimeRecord);
            }

            // 发送报告
            if (result) {
                sendReport(report);
            }

            return result;
        } finally {
            // 检查完置为初始值
            switchOpenDurationTimeRecord.clear();
            switchOpenTokenNotAcquiredRecord.clear();
        }
    }


}
