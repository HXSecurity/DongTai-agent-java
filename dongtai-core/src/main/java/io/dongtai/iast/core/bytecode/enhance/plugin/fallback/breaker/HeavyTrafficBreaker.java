package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.FallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.HeavyTrafficRateLimitReport;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * 高频流量熔断器实现(仅支持JDK8+)
 *
 * @author liyuan40
 * @date 2022/3/3 15:23
 */
public class HeavyTrafficBreaker extends AbstractBreaker {

    private static AbstractBreaker instance;

    private static CircuitBreaker breaker;

    private static Properties cfg;

    private HeavyTrafficBreaker(Properties cfg) {
        super(cfg);
    }

    public static AbstractBreaker newInstance(Properties cfg) {
        if (instance == null) {
            instance = new HeavyTrafficBreaker(cfg);
        }
        return instance;
    }

    @Override
    protected void initBreaker(Properties cfg) {
        HeavyTrafficBreaker.cfg = cfg;
        final int breakerWaitDuration = RemoteConfigUtils.getHeavyTrafficBreakerWaitDuration(cfg);
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastHeavyTrafficBreaker", CircuitBreakerConfig.custom()
                // 基于次数的滑动窗口
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 窗口大小固定为 1，因为在进入断路器前使用了令牌桶限流，故超过限流速度即打开高频流量断路器
                .slidingWindowSize(1)
                //失败率阈值百分比(>=50%)
                .failureRateThreshold(50F)
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(1)
                //自动从开启变成半开，等待时间从配置中读取
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(breakerWaitDuration))
                // 半开时允许通过次数，固定为 1
                .permittedNumberOfCallsInHalfOpenState(1)
                // 关注的失败的返回类型
                .recordResult(Predicate.isEqual(false))
                .build());
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    double trafficLimitRate = EngineManager.getFallbackManager().getHeavyTrafficRateLimiter().getTokenPerSecond();
                    // 断路器转为打开时，打开请求降级开关
                    CircuitBreaker.State state = event.getStateTransition().getToState();
                    if (state == CircuitBreaker.State.OPEN) {
                        FallbackSwitch.setHeavyTrafficLimitFallback(true);
                        HeavyTrafficRateLimitReport.sendReport(trafficLimitRate);
                    }
                    // 因为本断路器的样本来自流量，打开后无法获取新样本，故需要在 HALF_OPEN 状态直接转到 CLOSE 状态
                    if (state == CircuitBreaker.State.HALF_OPEN) {
                        breaker.transitionToClosedState();
                    }
                    // 关闭或半开断路器则关闭请求降级开关
                    if (state == CircuitBreaker.State.CLOSED) {
                        FallbackSwitch.setHeavyTrafficLimitFallback(false);
                    }
                });
        HeavyTrafficBreaker.breaker = breaker;
    }

    @Override
    public void breakCheck(String contextString) {
        if (breaker == null) {
            DongTaiLog.info("the HeavyTrafficBreaker need to be init,skip check.");
            return;
        }
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> false)).recover(throwable -> false).get();
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

}
