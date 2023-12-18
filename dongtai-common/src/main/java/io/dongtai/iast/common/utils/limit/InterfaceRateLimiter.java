package io.dongtai.iast.common.utils.limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import java.time.Duration;

/**
 * @author mazepeng
 * @date 2023/12/5 11:53
 * 接口速率限制器，用来限制接口请求的速率
 * 通过启动参数来开启此配置
 */
public class InterfaceRateLimiter {

    private final InterfaceRateLimiterSoftReferenceHashMap<String> buckets = new InterfaceRateLimiterSoftReferenceHashMap<>();

    private int theNumberOfTokenBucketPools;


    //最高速率
    private long rateCaps;


    private InterfaceRateLimiter(long rateCaps, int theNumberOfTokenBucketPools) {
        this.rateCaps = rateCaps;
        this.theNumberOfTokenBucketPools = theNumberOfTokenBucketPools;
    }

    public static InterfaceRateLimiter getInstance(long rateCaps, int theNumberOfTokenBucketPools) {
        return new InterfaceRateLimiter(rateCaps, theNumberOfTokenBucketPools);
    }

    /**
     * 接口通过判断
     *
     * @param interfaceName api接口名称
     * @return true 放行 false 拦截不采集
     * 暂时得实现为1秒钟的时间内的最高速率
     */
    public boolean whetherItPassesOrNot(String interfaceName) {
        Bucket bucket = null;
        if (buckets.containsKey(interfaceName)) {
            bucket = buckets.get(interfaceName);
            if (bucket == null) {
                return true;
            }
        } else {
            if (buckets.size() >= theNumberOfTokenBucketPools) {
                return true;
            }
            bucket = Bucket.builder().addLimit(Bandwidth.simple(rateCaps, Duration.ofSeconds(1))).build();
            buckets.put(interfaceName, bucket);
        }
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        return probe.isConsumed();
    }

    /**
     * 更新速率限制器的配置
     * @param rateCaps 速率
     * @param theNumberOfTokenBucketPools 桶池大小
     * 只有当值发生改变时，才会开启更新，否则不执行操作
     */
    public void updateTheData(long rateCaps, int theNumberOfTokenBucketPools) {
        if (this.rateCaps != rateCaps){
            this.rateCaps = rateCaps;
            buckets.updateTheData(this.rateCaps);
        }
        if (this.theNumberOfTokenBucketPools != theNumberOfTokenBucketPools) {
            this.theNumberOfTokenBucketPools = theNumberOfTokenBucketPools;
            //需要更新桶池大小时，判断已有的是否超过最大限制，如果超过，则删除到指定大小
            if (theNumberOfTokenBucketPools - buckets.size() < 0){
                int i = buckets.size() - theNumberOfTokenBucketPools;
                buckets.deleteTheElement(i);
            }
        }
    }
}
