package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.breaker;

import io.dongtai.log.DongTaiLog;

import java.util.Properties;

/**
 * 断路器抽象
 *
 * @author chenyi
 * @date 2022/3/3
 */
public abstract class AbstractBreaker {

    protected AbstractBreaker(Properties cfg) {
        initBreaker(cfg);
    }

    /**
     * 性能检查(由agent监控线程触发)
     *
     * @param contextString 上下文字符串
     */
    public static void checkPerformance(String contextString) {
        DongTaiLog.info("No suitable Performance Breaker,skip check.");
    }


    /**
     * 初始化断路器
     *
     * @param cfg 配置
     */
    protected abstract void initBreaker(Properties cfg);

}