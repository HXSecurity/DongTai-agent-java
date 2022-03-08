package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;


import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.AbstractBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.DefaultPerformanceBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.RequestBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.LimitFallbackSwitch;
import io.dongtai.iast.core.utils.global.RequestRateLimiter;
import io.dongtai.iast.core.utils.global.SwitchRateLimiter;
import io.dongtai.iast.core.utils.threadlocal.RateLimiterThreadLocal;

import java.util.Properties;

/**
 * 限制器管理器
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class LimiterManager {

    private static LimiterManager instance;
    /**
     * 性能断路器
     */
    private final AbstractBreaker performanceBreaker;

    /**
     * 请求断路器
     */
    private final AbstractBreaker requestBreaker;

    /**
     * hook点高频命中限速器
     */
    private final RateLimiterThreadLocal hookRateLimiter;

    /**
     * 高频请求限速器
     */
    private final RequestRateLimiter requestRateLimiter;

    /**
     * 二次降级熔断器的熔断限速器
     */
    private final SwitchRateLimiter switchRateLimiter;

    /**
     * 二次降级开关
     */
    private final LimitFallbackSwitch limitFallbackSwitch;

    public static LimiterManager newInstance(Properties cfg) {
        if (instance == null) {
            instance = new LimiterManager(cfg);
        }
        return instance;
    }

    private LimiterManager(Properties cfg) {
        this.performanceBreaker = DefaultPerformanceBreaker.newInstance(cfg);
        this.requestBreaker = RequestBreaker.newInstance(cfg);
        this.hookRateLimiter = new RateLimiterThreadLocal(cfg);
        this.requestRateLimiter = new RequestRateLimiter(cfg);
        this.switchRateLimiter = new SwitchRateLimiter(cfg);
        this.limitFallbackSwitch = new LimitFallbackSwitch(cfg);
    }

    public AbstractBreaker getPerformanceBreaker() {
        return performanceBreaker;
    }

    public RateLimiterThreadLocal getHookRateLimiter() {
        return hookRateLimiter;
    }

    public RequestRateLimiter getRequestRateLimiter() {
        return requestRateLimiter;
    }

    public SwitchRateLimiter getSwitchRateLimiter() {
        return switchRateLimiter;
    }

    public LimitFallbackSwitch getLimitFallbackSwitch() {
        return limitFallbackSwitch;
    }
}
