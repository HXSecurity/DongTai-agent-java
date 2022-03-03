package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;


import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.AbstractBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.DefaultPerformanceBreaker;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.threadlocal.RateLimiterThreadLocal;

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
     * hook点高频命中限速器
     */
    private final RateLimiterThreadLocal hookRateLimiter;

    public static LimiterManager newInstance(PropertyUtils cfg) {
        //todo add properties
        if (instance == null) {
            instance = new LimiterManager(cfg);
        }
        return instance;
    }

    private LimiterManager(PropertyUtils cfg) {
        this.performanceBreaker = DefaultPerformanceBreaker.newInstance(cfg);
        this.hookRateLimiter = new RateLimiterThreadLocal(cfg);
    }

    public AbstractBreaker getPerformanceBreaker() {
        return performanceBreaker;
    }

    public RateLimiterThreadLocal getHookRateLimiter() {
        return hookRateLimiter;
    }
}
