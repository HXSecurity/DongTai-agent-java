package io.dongtai.iast.agent.fallback;


import io.dongtai.iast.agent.fallback.breaker.AbstractBreaker;
import io.dongtai.iast.agent.fallback.breaker.PerformanceBreaker;
import io.dongtai.log.DongTaiLog;

import java.util.Properties;

/**
 * 降级管理器
 *
 * @author chenyi
 * @date 2022/3/3
 */
public class FallbackManager {
    /**
     * 降级管理器实例
     */
    private static FallbackManager instance;
    /**
     * 性能断路器
     */
    private final AbstractBreaker performanceBreaker;

    public static FallbackManager newInstance(Properties cfg) {
        if (instance == null) {
            instance = new FallbackManager(cfg);
        }
        return instance;
    }

    private FallbackManager(Properties cfg) {
        // 创建断路器实例
        this.performanceBreaker = PerformanceBreaker.newInstance(cfg);
    }

    public AbstractBreaker getPerformanceBreaker() {
        return performanceBreaker;
    }

    /**
     * 触发断路检查(由agent监控线程触发)
     *
     * @param contextString 上下文字符串
     */
    public static void invokePerformanceBreakerCheck(String contextString) {
        if (instance == null || instance.performanceBreaker == null) {
            DongTaiLog.info("No suitable performance breaker, skip check.");
            return;
        }
        instance.performanceBreaker.breakCheck(contextString);
    }
}
