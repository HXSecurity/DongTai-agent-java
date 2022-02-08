package com.secnium.iast.core.threadlocalpool;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastResponseCache extends ThreadLocal<Object> {
    @Override
    protected Object initialValue() {
        return null;
    }
}
