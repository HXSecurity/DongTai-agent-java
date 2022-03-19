package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.limiter;

/**
 * 限速器抽象类
 *
 * @author liyuan40
 * @date 2022/3/10 15:06
 */
public abstract class AbstractRateLimiter {

    /**
     * 获取令牌
     *
     * @return boolean 是否获取成功
     */
    public abstract boolean acquire();
}
