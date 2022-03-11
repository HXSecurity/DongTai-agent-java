package io.dongtai.iast.core.bytecode.enhance.plugin.fallback.limiter;

import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.RateLimiterWithCapacity;
import io.dongtai.iast.core.utils.config.RemoteConfigUtils;

import java.util.Properties;

/**
 * 高频流量限速器
 *
 * @author liyuan40
 * @date 2022/3/2 11:15
 */
public class HeavyTrafficRateLimiter extends AbstractRateLimiter {
    /**
     * 默认每次尝试获取的许可数
     */
    private static final int DEFAULT_PERMITS = 1;

    private final RateLimiter rateLimiter;

    public HeavyTrafficRateLimiter(Properties properties) {
        rateLimiter = RateLimiterWithCapacity.createSmoothBurstyLimiter(
                RemoteConfigUtils.getHeavyTrafficLimitTokenPerSecond(properties),
                RemoteConfigUtils.getHeavyTrafficLimitInitBurstSeconds(properties)
        );
    }

    public double getTokenPerSecond() {
        return RemoteConfigUtils.getHeavyTrafficLimitTokenPerSecond(null);
    }

    /**
     * 获取限速器速率
     *
     * @return double 速率
     */
    public double getRate() {
        return rateLimiter.getRate();
    }

    @Override
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
