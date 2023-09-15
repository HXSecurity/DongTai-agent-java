package io.dongtai.iast.core.utils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HashCode {
    public static long calc(Object obj) {
        if (obj instanceof String) {
            return obj.hashCode();
        } else {
            return System.identityHashCode(obj);
        }
    }
}
