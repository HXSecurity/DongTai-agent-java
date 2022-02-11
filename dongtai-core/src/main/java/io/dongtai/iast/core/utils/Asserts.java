package io.dongtai.iast.core.utils;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class Asserts {
    public static void NOT_NULL(String name, Object value) {
        if (value == null) {
            throw new NullPointerException(name);
        } else if (value instanceof String) {
            if (((String) value).isEmpty()) {
                throw new NullPointerException(name);
            }
        }
    }
}
