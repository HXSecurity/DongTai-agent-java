package io.dongtai.iast.core.bytecode.enhance.plugin.fallback;


import io.dongtai.iast.common.utils.version.JavaVersionUtils;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker.AbstractBreaker;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.limiter.HeavyTrafficRateLimiter;
import io.dongtai.iast.core.bytecode.enhance.plugin.fallback.limiter.FallbackSwitchFrequencyLimiter;
import io.dongtai.iast.core.utils.threadlocal.RateLimiterThreadLocal;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * 降级管理器
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class FallbackManager {

    private static final String BREAKER_NAMESPACE = "io.dongtai.iast.core.bytecode.enhance.plugin.fallback.breaker.";
    private static final String NOP_BREAKER_CLASS = BREAKER_NAMESPACE + "NopBreaker";
    private static final String PERFORMANCE_BREAKER_CLASS = BREAKER_NAMESPACE + "PerformanceBreaker";
    private static final String HEAVY_TRAFFIC_BREAKER_CLASS = BREAKER_NAMESPACE + "HeavyTrafficBreaker";
    /**
     * 降级管理器实例
     */
    private static FallbackManager instance;
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
    private final FallbackSwitchFrequencyLimiter fallbackSwitchFrequencyLimiter;

    public static FallbackManager newInstance(Properties cfg) {
        if (instance == null) {
            instance = new FallbackManager(cfg);
        }
        return instance;
    }

    private FallbackManager(Properties cfg) {
        // 创建断路器实例
        if (JavaVersionUtils.isJava6() || JavaVersionUtils.isJava7()) {
            this.performanceBreaker = (AbstractBreaker) invoke2CreateNewInstance(NOP_BREAKER_CLASS, cfg);
            this.heavyTrafficBreaker = (AbstractBreaker) invoke2CreateNewInstance(NOP_BREAKER_CLASS, cfg);
        } else {
            this.performanceBreaker = (AbstractBreaker) invoke2CreateNewInstance(PERFORMANCE_BREAKER_CLASS, cfg);
            this.heavyTrafficBreaker = (AbstractBreaker) invoke2CreateNewInstance(HEAVY_TRAFFIC_BREAKER_CLASS, cfg);
        }
        // 创建限速器实例
        this.hookRateLimiter = new RateLimiterThreadLocal(cfg);
        this.heavyTrafficRateLimiter = new HeavyTrafficRateLimiter(cfg);
        this.fallbackSwitchFrequencyLimiter = new FallbackSwitchFrequencyLimiter(cfg);
    }

    /**
     * 反射调用创建新实例
     */
    private Object invoke2CreateNewInstance(String clazzFullName, Properties cfg) {
        try {
            Class<?> clazz = Class.forName(clazzFullName);
            Method method = clazz.getMethod("newInstance", Properties.class);
            return method.invoke(null, cfg);
        } catch (Exception e) {
            DongTaiLog.error(e);
            return null;
        }
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

    public AbstractBreaker getHeavyTrafficBreaker() {
        return heavyTrafficBreaker;
    }

    public FallbackSwitchFrequencyLimiter getFallbackSwitchFrequencyLimiter() {
        return fallbackSwitchFrequencyLimiter;
    }

    /**
     * 触发断路检查(由agent监控线程触发)
     *
     * @param contextString 上下文字符串
     */
    public static void invokePerformanceBreakerCheck(String contextString) {
        if (instance == null || instance.performanceBreaker == null) {
            DongTaiLog.info("No suitable breaker,skip check.");
            return;
        }
        instance.performanceBreaker.breakCheck(contextString);
    }

    /**
     * 触发开关断路器(由agent监控线程触发)
     *
     * @param turnOn 打开
     */
    public static void invokeSwitchPerformanceBreaker(boolean turnOn) {
        if (instance == null || instance.performanceBreaker == null) {
            DongTaiLog.info("No suitable breaker,skip switch.");
            return;
        }
        instance.performanceBreaker.switchBreaker(turnOn);
    }

    /**
     * 判断是否需要二次降级(由agent监控线程触发)
     *
     * @return boolean
     */
    public static boolean isNeedSecondFallback() {
        return FallbackSwitch.isNeedSecondFallback();
    }
}
