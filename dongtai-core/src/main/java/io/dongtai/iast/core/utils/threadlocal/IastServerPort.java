package io.dongtai.iast.core.utils.threadlocal;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastServerPort extends ThreadLocal<Integer> {
    @Override
    protected Integer initialValue() {
        return null;
    }
}
