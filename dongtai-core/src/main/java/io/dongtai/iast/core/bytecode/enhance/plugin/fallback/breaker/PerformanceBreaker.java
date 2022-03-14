package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.IPerformanceChecker;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.checker.MetricsBindCheckerEnum;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.FallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.PerformanceLimitReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.body.PerformanceBreakReportBody;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;
import io.dongtai.iast.core.utils.json.GsonUtils;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.*;

/**
 * 性能熔断器实现(仅支持JDK8+)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class PerformanceBreaker extends AbstractBreaker {

    private static AbstractBreaker instance;

    private static CircuitBreaker breaker;

    private static Properties cfg;

    private PerformanceBreaker(Properties cfg) {
        super(cfg);
    }

    public static AbstractBreaker newInstance(Properties cfg) {
        if (instance == null) {
            instance = new PerformanceBreaker(cfg);
        }
        return instance;
    }

    @Override
    public void breakCheck(String contextString) {
        if (breaker == null) {
            DongTaiLog.info("the breaker need to be init,skip check.");
            return;
        }
        if (!RemoteConfigUtils.enableAutoFallback()) {
            return;
        }
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> checkMetricsWithAutoFallback(contextString)))
                .recover(throwable -> {
                    DongTaiLog.info(throwable.getMessage());
                    return false;
                }).get();
    }

    @Override
    public void switchBreaker(boolean turnOn) {
        if (breaker == null) {
            DongTaiLog.info("the breaker need to be init,skip switch.");
            return;
        }
        if (turnOn) {
            breaker.transitionToOpenState();
        } else {
            breaker.transitionToClosedState();
        }
    }

    @Override
    protected void initBreaker(Properties cfg) {
        PerformanceBreaker.cfg = cfg;
        final Integer breakerWindowSize = RemoteConfigUtils.getPerformanceBreakerWindowSize(cfg);
        final Double breakerFailureRate = RemoteConfigUtils.getPerformanceBreakerFailureRate(cfg);
        final Integer breakerWaitDuration = RemoteConfigUtils.getPerformanceBreakerWaitDuration(cfg);
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastPerformanceBreaker", CircuitBreakerConfig.custom()
                // 基于次数的滑动窗口(默认窗口大小2)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(breakerWindowSize)
                //失败率阈值百分比(默认>=51%)
                .failureRateThreshold(breakerFailureRate.floatValue())
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(breakerWindowSize)
                //自动从开启变成半开(默认等待40秒)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(breakerWaitDuration))
                // 半开时允许通过次数(默认窗口大小*5)
                .permittedNumberOfCallsInHalfOpenState(breakerWindowSize * 5)
                // 关注的失败异常类型
                .recordExceptions(IllegalStateException.class)
                .build());
        // 断路器事件监听
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    final CircuitBreaker.State toState = event.getStateTransition().getToState();
                    if (toState == CircuitBreaker.State.OPEN) {
                        PerformanceLimitReport.sendReport();
                        FallbackSwitch.setPerformanceFallback(true);
                    } else if (toState == CircuitBreaker.State.CLOSED) {
                        FallbackSwitch.setPerformanceFallback(false);
                    }
                });
        PerformanceBreaker.breaker = breaker;
    }

    private static boolean checkMetricsWithAutoFallback(String contextString) {
        List<PerformanceMetrics> performanceMetrics = convert2MetricsList(contextString);
        // 检查每个性能是否达到风险值
        int riskMetricsCount = 0;
        final Integer performanceLimitRiskMaxMetricsCount = RemoteConfigUtils.getPerformanceLimitRiskMaxMetricsCount(cfg);
        if (performanceLimitRiskMaxMetricsCount > 0) {
            for (PerformanceMetrics metrics : performanceMetrics) {
                final IPerformanceChecker performanceChecker = MetricsBindCheckerEnum.newCheckerInstance(metrics.getMetricsKey());
                if (performanceChecker != null && performanceChecker.isPerformanceRisk(metrics, cfg)) {
                    riskMetricsCount++;
                }
                //达到性能风险的指标数量超过阈值
                if (riskMetricsCount >= performanceLimitRiskMaxMetricsCount) {
                    final PerformanceMetrics threshold = performanceChecker.getMatchRiskThreshold(metrics.getMetricsKey(), cfg);
                    appendToOverThresholdLog(true, metrics, threshold, riskMetricsCount);
                    throw new IllegalStateException("performance is over risk threshold! riskMetricsCount:" + riskMetricsCount);
                }
            }
        }
        // 检查每个性能是否达到限制值
        for (PerformanceMetrics metrics : performanceMetrics) {
            final IPerformanceChecker performanceChecker = MetricsBindCheckerEnum.newCheckerInstance(metrics.getMetricsKey());
            if (performanceChecker != null && performanceChecker.isPerformanceOverLimit(metrics, cfg)) {
                final PerformanceMetrics threshold = performanceChecker.getMatchMaxThreshold(metrics.getMetricsKey(), cfg);
                appendToOverThresholdLog(false, metrics, threshold, 1);
                throw new IllegalStateException("performance is over max threshold! metrics:" + GsonUtils.toJson(metrics));
            }
        }
        return true;
    }

    /**
     * 将上下文转换为指标列表
     *
     * @param contextString 上下文字符串
     * @return {@link List}<{@link PerformanceMetrics}> 指标列表
     */
    private static List<PerformanceMetrics> convert2MetricsList(String contextString) {
        try {
            final List<Class<?>> clazzWhiteList = Arrays.asList(PerformanceMetrics.class, MetricsKey.class,
                    CpuInfoMetrics.class, MemoryUsageMetrics.class, GarbageInfoMetrics.class, GarbageInfoMetrics.CollectionInfo.class,
                    ThreadInfoMetrics.class, ThreadInfoMetrics.ThreadInfo.class);
            return SerializeUtils.deserialize2ArrayList(contextString, PerformanceMetrics.class, clazzWhiteList);
        } catch (Exception e) {
            DongTaiLog.warn("convert2MetricsList failed, err:{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 追加性能超限日志记录
     *
     * @param isRisk       是否是风险阈值
     * @param nowMetrics   当前指标
     * @param threshold    阈值指标
     * @param metricsCount 超限的指标数
     */
    private static void appendToOverThresholdLog(boolean isRisk, PerformanceMetrics nowMetrics, PerformanceMetrics threshold, Integer metricsCount) {
        final PerformanceBreakReportBody.PerformanceOverThresholdLog breakLog = new PerformanceBreakReportBody.PerformanceOverThresholdLog();
        breakLog.setDate(new Date());
        breakLog.setOverThresholdType(isRisk ? 1 : 2);
        breakLog.setNowMetrics(nowMetrics);
        breakLog.setThreshold(threshold);
        breakLog.setOverThresholdCount(metricsCount);
        PerformanceLimitReport.appendPerformanceBreakLog(breakLog);
    }

}
