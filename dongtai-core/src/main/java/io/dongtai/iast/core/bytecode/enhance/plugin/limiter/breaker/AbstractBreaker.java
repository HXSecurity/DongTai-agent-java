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

    public static AbstractBreaker newInstance(Properties cfg) {
        DongTaiLog.info("No suitable breaker,skip create newInstance.");
        return null;
    }

    protected AbstractBreaker(Properties cfg) {
        initBreaker(cfg);
    }

    /**
     * 触发断路检查(由agent监控线程触发)
     *
     * @param contextString 上下文字符串
     */
    public static void invokeBreakCheck(String contextString) {
        DongTaiLog.info("No suitable breaker,skip check.");
    }

    /**
     * 触发开关断路器(由agent监控线程触发)
     */
    public static void invokeSwitchBreaker(boolean turnOn) {
        DongTaiLog.info("No suitable breaker,skip switch.");
    }

    /**
     * 初始化断路器
     *
     * @param cfg 配置
     */
    protected abstract void initBreaker(Properties cfg);


    /**
     * 断路检查
     *
     * @param contextString 上下文字符串
     */
    public abstract void breakCheck(String contextString);

    /**
     * 开关断路器
     *
     * @param turnOn 打开/关闭断路器
     */
    public abstract void switchBreaker(boolean turnOn);

}
