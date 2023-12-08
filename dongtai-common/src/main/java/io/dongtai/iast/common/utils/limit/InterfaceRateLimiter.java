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

    private final InterfaceRateLimiterSoftReferenceHashMap<String, Bucket> buckets = new InterfaceRateLimiterSoftReferenceHashMap<>();


    //最高速率
    private final long rateCaps;


    private InterfaceRateLimiter(long rateCaps) {
        this.rateCaps = rateCaps;
    }

    public static InterfaceRateLimiter getInstance(long rateCaps) {
        return new InterfaceRateLimiter(rateCaps);
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
        }else {
            if (buckets.size() >= 5000){
                return true;
            }
            bucket = Bucket.builder().addLimit(Bandwidth.simple(rateCaps, Duration.ofSeconds(1))).build();
            buckets.put(interfaceName, bucket);
        }
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        return probe.isConsumed();
    }

}
