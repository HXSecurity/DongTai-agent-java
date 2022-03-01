package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.log.DongTaiLog;

/**
 * 性能熔断器空实现(该实现不会进行任何操作)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class NopPerformanceBreaker {

    public void checkPerformance(String context) {
        DongTaiLog.info("No suitable Performance Breaker,skip check.");
    }
}
