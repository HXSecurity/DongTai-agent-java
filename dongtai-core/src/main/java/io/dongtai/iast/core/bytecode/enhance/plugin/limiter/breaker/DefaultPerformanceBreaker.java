package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.iast.common.entity.performance.metrics.*;
import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认的性能熔断器实现(仅支持JDK8+)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class DefaultPerformanceBreaker extends AbstractBreaker {

    private static AbstractBreaker instance;

    private static CircuitBreaker breaker;

    private static PropertyUtils cfg;

    private DefaultPerformanceBreaker(PropertyUtils cfg) {
        super(cfg);
    }

    public static AbstractBreaker newInstance(PropertyUtils cfg) {
        if (instance == null) {
            instance = new DefaultPerformanceBreaker(cfg);
        }
        return instance;
    }

    /**
     * 性能检查(由agent监控线程触发)
     *
     * @param contextString 上下文字符串
     */
    public static void checkPerformance(String contextString) {
        if (breaker == null) {
            DongTaiLog.info("the breaker need to be init,skip check.");
            return;
        }
        Try.ofSupplier(CircuitBreaker.decorateSupplier(breaker, () -> checkMetricsWithAutoFallback(contextString)))
                .recover(throwable -> {
                    //todo fallback
                    System.out.println("执行降级方法" + throwable);
                    return false;
                }).get();
    }

    @Override
    protected void initBreaker(PropertyUtils cfg) {
        DefaultPerformanceBreaker.cfg = cfg;
        // 创建断路器自定义配置
        CircuitBreaker breaker = CircuitBreaker.of("iastPerformanceBreaker", CircuitBreakerConfig.custom()
                // 基于次数的滑动窗口,窗口大小2
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                //失败率阈值百分比(>=51%)
                .failureRateThreshold(50F)
                //计算失败率或慢调用率之前所需的最小调用数
                .minimumNumberOfCalls(2)
                //自动从开启变成半开，等待30秒
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                // 半开时允许通过次数
                .permittedNumberOfCallsInHalfOpenState(10)
                // 关注的失败异常类型
                .recordExceptions(IllegalStateException.class)
                //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
                .build());
        breaker.getEventPublisher()
                .onStateTransition(event -> {
                    //todo
                    System.out.println("断路器状态转换:" + event.getStateTransition().getFromState() + "->" + event.getStateTransition().getToState());
                });
        DefaultPerformanceBreaker.breaker = breaker;
    }

    private static boolean checkMetricsWithAutoFallback(String contextString) {
        List<PerformanceMetrics> performanceMetrics = convert2MetricsList(contextString);
        // 风险阈值检查
        performanceMetrics.forEach((metrics) -> {
            //todo allCheck
        });
        // 最大阈值检查
        performanceMetrics.forEach((metrics) -> {
            if (metrics.getMetricsKey() == MetricsKey.CPU_USAGE) {
                Double cpuUsage = metrics.getMetricsValue(Double.class);
                //todo check
                if (cpuUsage > 35) {
                    throw new IllegalStateException("cpu limit:" + cpuUsage);
                }
            }
            System.out.println(metrics.getMetricsKey().toString());
            System.out.println(metrics.getMetricsValue(metrics.getMetricsKey().getValueType()));
        });
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
                    MemoryUsageMetrics.class, GarbageInfoMetrics.class, GarbageInfoMetrics.CollectionInfo.class, ThreadInfoMetrics.class);
            return SerializeUtils.deserialize2ArrayList(contextString, PerformanceMetrics.class, clazzWhiteList);
        } catch (Exception e) {
            DongTaiLog.warn("convert2MetricsList failed, err:{}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
