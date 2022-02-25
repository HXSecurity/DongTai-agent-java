package io.dongtai.iast.core.utils.threadlocal;


import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.RateLimiterWithCapacity;

/**
 * 本地线程隔离限速器
 *
 * @author chenyi
 * @date 2022/2/25
 */
public class RateLimiterThreadLocal extends ThreadLocal<RateLimiter> {
    /**
     * 默认每次尝试获取的许可数
     */
    public static final int DEFAULT_PERMITS = 1;
    /**
     * 每秒颁发令牌速率
     */
    double tokenPerSecond;
    /**
     * 初始预放置令牌时间
     */
    double initBurstSeconds;

    public RateLimiterThreadLocal(double tokenPerSecond, double initBurstSeconds) {
        this.tokenPerSecond = tokenPerSecond;
        this.initBurstSeconds = initBurstSeconds;
    }

    @Override
    protected RateLimiter initialValue() {
        return RateLimiterWithCapacity.createSmoothBurstyLimiter(tokenPerSecond, initBurstSeconds);
    }

    /**
     * 尝试获取令牌(非阻塞)
     *
     * @return 是否获取成功
     */
    public boolean acquire() {
        return acquire(DEFAULT_PERMITS);
    }

    /**
     * 尝试获取令牌(非阻塞)
     *
     * @param permits 许可数
     * @return 是否获取成功
     */
    public boolean acquire(int permits) {
        return this.get().tryAcquire(permits);
    }

}
