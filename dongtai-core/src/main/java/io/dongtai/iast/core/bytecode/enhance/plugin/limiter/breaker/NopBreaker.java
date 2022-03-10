package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;


import java.util.Properties;

/**
 * 熔断器空实现(该实现不会进行任何操作)
 *
 * @author chenyi
 * @date 2022/2/28
 */
public class NopBreaker extends AbstractBreaker {

    protected NopBreaker(Properties cfg) {
        super(cfg);
    }

    @Override
    protected void initBreaker(Properties cfg) {
    }
}
