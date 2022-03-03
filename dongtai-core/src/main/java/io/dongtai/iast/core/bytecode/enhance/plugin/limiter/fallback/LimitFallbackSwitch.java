package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;

/**
 * 限制降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class LimitFallbackSwitch {

    private LimitFallbackSwitch() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 高频hook点降级开关(线程隔离)
     */
    private static final BooleanThreadLocal HEAVY_HOOK_FALLBACK = new BooleanThreadLocal(false);
    /**
     * 大流量降级开关
     */
    private static boolean HEAVY_TRAFFIC_LIMIT_FALLBACK = false;
    /**
     * 性能降级开关
     */
    private static boolean PERFORMANCE_FALLBACK = false;

    /**
     * 异常降级开关
     */
    private static boolean EXCEPTION_FALLBACK = false;


    /**
     * 请求对当前请求降级
     *
     * @return boolean 是否发生降级
     */
    public static boolean isRequestFallback() {
        return HEAVY_HOOK_FALLBACK.get() != null && HEAVY_HOOK_FALLBACK.get();
    }

    /**
     * 是否对全局增强点降级
     *
     * @return boolean 是否发生降级
     */
    public static boolean isGlobalFallback() {
        return HEAVY_TRAFFIC_LIMIT_FALLBACK || PERFORMANCE_FALLBACK || EXCEPTION_FALLBACK;
    }


    public boolean getHeavyTrafficLimitFallback() {
        return HEAVY_TRAFFIC_LIMIT_FALLBACK;
    }

    public boolean getPerformanceFallback() {
        return PERFORMANCE_FALLBACK;
    }

    public boolean getExceptionFallback() {
        return EXCEPTION_FALLBACK;
    }

    public static void setHeavyHookFallback(boolean fallback) {
        HEAVY_HOOK_FALLBACK.set(true);
    }

    public static void clearHeavyHookFallback() {
        HEAVY_HOOK_FALLBACK.remove();
    }

    public static void setHeavyTrafficLimitFallback(boolean fallback) {
        HEAVY_TRAFFIC_LIMIT_FALLBACK = fallback;
    }

    public static void setPerformanceFallback(boolean fallback) {
        PERFORMANCE_FALLBACK = fallback;
    }

    public static void setExceptionFallback(boolean fallback) {
        EXCEPTION_FALLBACK = fallback;
    }


}
