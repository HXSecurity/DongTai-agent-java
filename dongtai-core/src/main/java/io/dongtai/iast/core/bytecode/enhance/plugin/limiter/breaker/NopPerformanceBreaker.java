package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.iast.core.utils.PropertyUtils;

/**
 * 性能熔断器空实现(该实现不会进行任何操作)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class NopPerformanceBreaker extends AbstractBreaker {

    protected NopPerformanceBreaker(PropertyUtils cfg) {
        super(cfg);
    }

    @Override
    protected void initBreaker(PropertyUtils cfg) {
    }
}
