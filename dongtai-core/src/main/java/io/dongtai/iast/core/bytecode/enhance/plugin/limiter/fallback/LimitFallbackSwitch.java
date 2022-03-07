package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;

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
     * 是否对引擎降级(全局增强点生效)
     *
     * @return boolean 是否发生降级
     */
    public static boolean isEngineFallback() {
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
        if(fallback){
            DongTaiLog.info("Engine performance fallback is open, Engine shut down successfully");
            PERFORMANCE_FALLBACK = true;
        }else{
            DongTaiLog.info("Engine performance fallback is close, Engine opened successfully");
            PERFORMANCE_FALLBACK = false;
        }
    }

    public static void setExceptionFallback(boolean fallback) {
        EXCEPTION_FALLBACK = fallback;
    }


}
