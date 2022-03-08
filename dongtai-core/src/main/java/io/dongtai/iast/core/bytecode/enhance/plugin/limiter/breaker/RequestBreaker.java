package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.LimitFallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.RequestRateLimitReport;
import io.dongtai.iast.core.utils.RemoteConfigUtils;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * 请求断路器
 *
 * @author liyuan40
 * @date 2022/3/3 15:23
 */
public class RequestBreaker extends AbstractBreaker {

    private static AbstractBreaker instance;

    private static CircuitBreaker breaker;

    private static Properties cfg;

    private RequestBreaker(Properties cfg) {
        super(cfg);
    }

    public static AbstractBreaker newInstance(Properties cfg) {
        if (instance == null) {
            instance = new RequestBreaker(cfg);
        }
        return instance;
    }

    @Override
    protected void initBreaker(Properties cfg) {
        RequestBreaker.cfg = cfg;
        final int breakerWaitDuration = RemoteConfigUtils.getRequestWaitDurationInOpenState(cfg);
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastRequestBreaker", CircuitBreakerConfig.custom()
                // 基于次数的滑动窗口
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 窗口大小固定为 1
                .slidingWindowSize(1)
                //失败率阈值百分比(>=50%)
                .failureRateThreshold(50F)
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(1)
                //自动从开启变成半开，等待时间 x 秒
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(breakerWaitDuration))
                // 半开时允许通过次数，固定为 1
                .permittedNumberOfCallsInHalfOpenState(1)
                // 关注的失败的返回类型
                .recordResult(Predicate.isEqual(false))
                .build());
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    DongTaiLog.info("RequestBreaker state transform:{}", event.getStateTransition());
                    double failureRateOnStateTransition = breaker.getMetrics().getFailureRate();
                    double failureRateThreshold = breaker.getCircuitBreakerConfig().getFailureRateThreshold();
                    // 断路器转为打开时，打开请求降级开关
                    CircuitBreaker.State state = event.getStateTransition().getToState();
                    if (state == CircuitBreaker.State.OPEN) {
                        setRequestFallback(true, failureRateOnStateTransition, failureRateThreshold);
                    }

                    // 断路器转为半开时，直接转为关闭
                    if (state == CircuitBreaker.State.HALF_OPEN) {
                        breaker.transitionToClosedState();
                    }

                    // 关闭或半开断路器则关闭请求降级开关
                    if (state == CircuitBreaker.State.CLOSED) {
                        setRequestFallback(false, failureRateOnStateTransition, failureRateThreshold);
                    }
                });
        RequestBreaker.breaker = breaker;

    }

    /**
     * 操作大流量降级开关
     *
     * @param fallback                     开关状态（true=ON,false=OFF）
     * @param failureRateOnStateTransition 触发本方法调用时的失败率
     * @param failureRateThreshold         失败率阈值
     */
    private static void setRequestFallback(boolean fallback, double failureRateOnStateTransition, double failureRateThreshold) {
        if (LimitFallbackSwitch.getHeavyTrafficLimitFallback() != fallback) {
            LimitFallbackSwitch.setHeavyTrafficLimitFallback(fallback);
            RequestRateLimitReport.sendReport(fallback, failureRateOnStateTransition, failureRateThreshold);
        }
    }

    /**
     * 检查请求速率
     */
    public static void checkRequestRate() {
        if (breaker == null) {
            DongTaiLog.info("the RequestBreaker need to be init,skip check.");
            return;
        }
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> false)).recover(throwable -> false).get();
    }

}
