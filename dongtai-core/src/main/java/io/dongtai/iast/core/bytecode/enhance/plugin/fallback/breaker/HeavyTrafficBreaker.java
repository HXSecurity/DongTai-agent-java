package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.FallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.report.HeavyTrafficRateLimitReport;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;
import io.dongtai.log.DongTaiLog;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.control.Try;

//import java.time.Duration;
import java.util.Properties;
//import java.util.function.Predicate;

/**
 * 高频流量熔断器实现(仅支持JDK8+)
 *
 * @author liyuan40
 * @date 2022/3/3 15:23
 */
public class HeavyTrafficBreaker extends AbstractBreaker {

    private static Properties cfg;

    private HeavyTrafficBreaker(Properties cfg) {
        super(cfg);
    }

    @Override
    protected void initBreaker(Properties cfg) {
    }

    @Override
    public void breakCheck(String contextString) {
    }

    @Override
    public void switchBreaker(boolean turnOn) {
    }

}
