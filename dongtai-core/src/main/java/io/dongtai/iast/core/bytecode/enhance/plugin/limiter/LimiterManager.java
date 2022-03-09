package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;


import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.AbstractBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.DefaultPerformanceBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.HeavyTrafficBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.bucket.HeavyTrafficRateLimiter;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.bucket.SwitchRateLimiter;
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
     * 高频流量断路器
     */
    private final AbstractBreaker heavyTrafficBreaker;

    /**
     * hook点高频命中限速器
     */
    private final RateLimiterThreadLocal hookRateLimiter;

    /**
     * 高频流量限速器
     */
    private final HeavyTrafficRateLimiter heavyTrafficRateLimiter;

    /**
     * 降级开关限速器
     */
    private final SwitchRateLimiter switchRateLimiter;

    public static LimiterManager newInstance(Properties cfg) {
        if (instance == null) {
            instance = new LimiterManager(cfg);
        }
        return instance;
    }

    private LimiterManager(Properties cfg) {
        this.performanceBreaker = DefaultPerformanceBreaker.newInstance(cfg);
        this.heavyTrafficBreaker = HeavyTrafficBreaker.newInstance(cfg);
        this.hookRateLimiter = new RateLimiterThreadLocal(cfg);
        this.heavyTrafficRateLimiter = new HeavyTrafficRateLimiter(cfg);
        this.switchRateLimiter = new SwitchRateLimiter(cfg);
    }

    public AbstractBreaker getPerformanceBreaker() {
        return performanceBreaker;
    }

    public RateLimiterThreadLocal getHookRateLimiter() {
        return hookRateLimiter;
    }

    public HeavyTrafficRateLimiter getHeavyTrafficRateLimiter() {
        return heavyTrafficRateLimiter;
    }

    public SwitchRateLimiter getSwitchRateLimiter() {
        return switchRateLimiter;
    }
}
