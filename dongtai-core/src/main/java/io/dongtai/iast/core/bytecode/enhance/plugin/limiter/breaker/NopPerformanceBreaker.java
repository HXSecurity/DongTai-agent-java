package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;


import java.util.Properties;

/**
 * 性能熔断器空实现(该实现不会进行任何操作)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class NopPerformanceBreaker extends AbstractBreaker {

    protected NopPerformanceBreaker(Properties cfg) {
        super(cfg);
    }

    @Override
    protected void initBreaker(Properties cfg) {
    }
}
