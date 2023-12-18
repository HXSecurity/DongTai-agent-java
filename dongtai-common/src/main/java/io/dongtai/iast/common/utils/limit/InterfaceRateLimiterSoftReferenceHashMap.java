package io.dongtai.iast.common.utils.limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mazepeng
 * @date 2023/12/7 16:56
 */
public class InterfaceRateLimiterSoftReferenceHashMap<K> {

    private final ConcurrentHashMap<K, SoftReference<Bucket>> map = new ConcurrentHashMap<>();

    public void put(K key, Bucket value) {
        map.put(key, new SoftReference<>(value));
    }

    /**
     * 更新令牌桶的速率限制
     * @param rateCaps 速率
     */
    public void updateTheData(long rateCaps){
        map.replaceAll((key, value) -> {
            Bucket build = Bucket.builder().addLimit(Bandwidth.simple(rateCaps, Duration.ofSeconds(1))).build();
            return new SoftReference<>(build);
        });
    }

    /**
     * 删除指定个桶
     * @param theNumberOfTokenBucketPools 删除的数量
     */
    public void deleteTheElement(int theNumberOfTokenBucketPools){
        AtomicInteger atomicInteger = new AtomicInteger(theNumberOfTokenBucketPools);
        for (Iterator<Map.Entry<K, SoftReference<Bucket>>> iterator = map.entrySet().iterator(); ; iterator.hasNext()) {
            Map.Entry<K, SoftReference<Bucket>> next = iterator.next();

            int i = atomicInteger.decrementAndGet();
            if (i >= 0) {
                iterator.remove();
            } else {
                // 满足终止条件时，跳出循环
                break;
            }
        }
    }

    public Bucket get(K key) {
        SoftReference<Bucket> softReference = map.get(key);
        if (softReference != null) {
            return softReference.get();
        }
        return null;
    }

    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    public int size(){
        return map.size();
    }

    public void remove(K key) {
        map.remove(key);
    }
}
