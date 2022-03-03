package com.google.common.util.concurrent;

/**
 * 可预置令牌的令牌桶限速器
 * SmoothRateLimiter仅于com.google.common.util.concurrent包内可见
 *
 * @author chenyi
 * @date 2022/2/21
 */
public class RateLimiterWithCapacity {

    /**
     * 创建应对突发流量的平滑限流器
     *
     * @param permitsPerSecond 每秒颁发令牌速率
     * @param maxBurstSeconds  初始预放置令牌时间
     * @return 限流器
     */
    public static RateLimiter createSmoothBurstyLimiter(double permitsPerSecond, double maxBurstSeconds) {
        // 配置突发流量平滑限流器
        SmoothRateLimiter.SmoothBursty rateLimiter = new SmoothRateLimiter.SmoothBursty(
                RateLimiter.SleepingStopwatch.createFromSystemTimer(), maxBurstSeconds);
        rateLimiter.setRate(permitsPerSecond);
        // 预置令牌
        rateLimiter.storedPermits = maxBurstSeconds * permitsPerSecond;
        return rateLimiter;
    }
}
