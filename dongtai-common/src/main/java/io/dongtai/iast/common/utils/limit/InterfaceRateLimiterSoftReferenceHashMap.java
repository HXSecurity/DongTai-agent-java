package io.dongtai.iast.common.utils.limit;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mazepeng
 * @date 2023/12/7 16:56
 */
public class InterfaceRateLimiterSoftReferenceHashMap<K,V> {

    private final ConcurrentHashMap<K, SoftReference<V>> map = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        map.put(key, new SoftReference<>(value));
    }

    public V get(K key) {
        SoftReference<V> softReference = map.get(key);
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
