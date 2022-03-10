package io.dongtai.iast.core.bytecode.enhance.plugin.limiter;


import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.AbstractBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.DefaultPerformanceBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker.NopPerformanceBreaker;
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
     * hook点高频命中限速器
     */
    private final RateLimiterThreadLocal hookRateLimiter;

    public static LimiterManager newInstance(Properties cfg) {
        if (instance == null) {
            instance = new LimiterManager(cfg);
        }
        return instance;
    }

    private LimiterManager(Properties cfg) {
        if (JavaVersionUtils.isJava6() || JavaVersionUtils.isJava7()) {
            this.performanceBreaker = NopPerformanceBreaker.newInstance(cfg);
        } else {
            this.performanceBreaker = DefaultPerformanceBreaker.newInstance(cfg);
        }
        this.hookRateLimiter = new RateLimiterThreadLocal(cfg);
    }

    public AbstractBreaker getPerformanceBreaker() {
        return performanceBreaker;
    }

    public RateLimiterThreadLocal getHookRateLimiter() {
        return hookRateLimiter;
    }
}
