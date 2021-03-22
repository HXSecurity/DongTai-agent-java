package com.secnium.iast.core.threadlocalpool;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class BooleanTheadLocal extends ThreadLocal<Boolean> {
    boolean defaultValue;

    public BooleanTheadLocal(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    protected Boolean initialValue() {
        return null;
    }

    public void enterHttpEntryPoint() {
        this.set(true);
    }

    public boolean isEnterHttp() {
        return this.get() != null && this.get();
    }
}
