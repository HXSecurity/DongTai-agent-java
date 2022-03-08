package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.LimitFallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.ExceptionLimitReport;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * 异常监控断路器
 *
 * @author liyuan40
 * @date 2022/3/7 11:57
 */
public class ExceptionBreaker extends AbstractBreaker {

    private static AbstractBreaker instance;

    private static CircuitBreaker breaker;

    private static Properties cfg;

    private ExceptionBreaker(Properties cfg) {
        super(cfg);
    }

    public static AbstractBreaker newInstance(Properties cfg) {
        if (instance == null) {
            instance = new ExceptionBreaker(cfg);
        }
        return instance;
    }

    @Override
    protected void initBreaker(Properties cfg) {
        ExceptionBreaker.cfg = cfg;
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastExceptionBreaker", CircuitBreakerConfig.custom()
                // 基于时间的滑动窗口
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 窗口大小固定为 1
                .slidingWindowSize(1)
                //失败率阈值百分比(>=50%)
                .failureRateThreshold(50F)
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(1)
                //自动从开启变成半开，等待 5 秒
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                // 半开时允许通过次数
                .permittedNumberOfCallsInHalfOpenState(1)
                // 关注的失败的返回类型
                .recordResult(Predicate.isEqual(false))
                .build());
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    DongTaiLog.info("异常限速断路器状态转换:{}", event.getStateTransition());
                    double failureRateOnStateTransition = breaker.getMetrics().getFailureRate();
                    double failureRateThreshold = breaker.getCircuitBreakerConfig().getFailureRateThreshold();
                    // 打开断路器则打开请求降级开关
                    CircuitBreaker.State state = event.getStateTransition().getToState();
                    if (state == CircuitBreaker.State.OPEN) {
                        setExceptionFallback(true, failureRateOnStateTransition, failureRateThreshold);
                    }
                    // 关闭或半开断路器则关闭请求降级开关
                    if (state == CircuitBreaker.State.CLOSED) {
                        setExceptionFallback(false, failureRateOnStateTransition, failureRateThreshold);
                    }
                });
        ExceptionBreaker.breaker = breaker;
    }

    /**
     * 设置异常熔断器
     *
     * @param fallback                     是否开启熔断器
     * @param failureRateOnStateTransition 触发本方法调用时的失败率
     * @param failureRateThreshold         失败率阈值
     */
    public static void setExceptionFallback(boolean fallback, double failureRateOnStateTransition, double failureRateThreshold) {
        if (LimitFallbackSwitch.getExceptionFallback() != fallback) {
//            LimitFallbackSwitch.setExceptionFallback(fallback);
            ExceptionLimitReport.sendReport(fallback, failureRateOnStateTransition, failureRateThreshold);
        }
    }

    /**
     * 检查异常
     *
     * @param exception 是否捕获到异常
     */
    public static void checkException(boolean exception) {
        if (breaker == null) {
            DongTaiLog.info("the ExceptionBreaker need to be init,skip check.");
            return;
        }
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> exception)).recover(throwable -> false).get();
    }

}
