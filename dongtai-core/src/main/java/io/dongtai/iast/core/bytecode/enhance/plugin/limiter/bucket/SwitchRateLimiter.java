package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.bucket;

import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.RateLimiterWithCapacity;
import io.dongtai.iast.core.utils.RemoteConfigUtils;

import java.util.Properties;

/**
 * 熔断器开关限速器
 *
 * @author liyuan40
 * @date 2022/3/7 18:10
 */
public class SwitchRateLimiter {
    /**
     * 默认每次尝试获取的许可数
     */
    public static final int DEFAULT_PERMITS = 1;

    RateLimiter rateLimiter;
    /**
     * 每秒颁发令牌速率
     */
    double tokenPerSecond;
    /**
     * 初始预放置令牌时间
     */
    double initBurstSeconds;

    public SwitchRateLimiter(Properties properties) {
        tokenPerSecond = RemoteConfigUtils.getSwitchLimitTokenPerSecond(properties);
        initBurstSeconds = RemoteConfigUtils.getSwitchLimitInitBurstSeconds(properties);
        rateLimiter = RateLimiterWithCapacity.createSmoothBurstyLimiter(tokenPerSecond, initBurstSeconds);
    }

    /**
     * 获取限速器速率
     *
     * @return double 速率
     */
    public double getRate() {
        return rateLimiter.getRate();
    }

    /**
     * 尝试获取令牌
     *
     * @return 是否获取成功
     */
    public boolean acquire() {
        // 未开启全局自动降级开关,不尝试获取令牌
        if (!RemoteConfigUtils.enableAutoFallback()) {
            return true;
        }
        return acquire(DEFAULT_PERMITS);
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 许可数
     * @return 是否获取成功
     */
    public boolean acquire(int permits) {
        return rateLimiter.tryAcquire(permits);
    }
}
