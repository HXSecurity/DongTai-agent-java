package io.dongtai.iast.core.bytecode.enhance.plugin.fallback;

import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.log.DongTaiLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;

/**
 * 降级开关
 *
 * @author chenyi
 * @date 2022/3/2
 */
public class FallbackSwitch {

    private FallbackSwitch() {
        throw new IllegalStateException("Utility class");
    }

    // *************************************************************
    // 降级开关配置
    // *************************************************************

    /**
     * 性能降级开关
     */
    @Getter
    @Setter
    private static boolean PERFORMANCE_FALLBACK = false;

    /**
     * 是否对引擎降级(全局增强点生效)
     *
     * @return boolean 是否发生降级
     */
    public static boolean isEngineFallback() {
        return PERFORMANCE_FALLBACK;
    }

    public static void setPerformanceFallback(boolean fallback) {
        PERFORMANCE_FALLBACK = fallback;
        DongTaiLog.info("Engine performance fallback is {}, Fallback engine {} successfully", fallback ? "open" : "close", fallback ? "shut down" : "opened");
    }



}
