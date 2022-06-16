package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.GarbageInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;
import io.dongtai.iast.common.entity.performance.metrics.ThreadInfoMetrics;
import io.dongtai.iast.common.enums.MetricsKey;
import io.dongtai.iast.common.utils.serialize.SerializeUtils;
import io.dongtai.iast.core.EngineManager;
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
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.core.EventConsumer;
import io.vavr.control.Try;

//import java.time.Duration;
import java.io.Serializable;
import java.util.*;
//import java.util.function.Function;
//import java.util.function.Supplier;

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
    }

    @Override
    public void switchBreaker(boolean turnOn) {
    }

    @Override
    protected void initBreaker(Properties cfg) {
    }

}
