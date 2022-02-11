package io.dongtai.iast.core.utils.threadlocal;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class BooleanThreadLocal extends ThreadLocal<Boolean> {

    boolean defaultValue;

    public BooleanThreadLocal(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    protected Boolean initialValue() {
        return defaultValue;
    }

    public void enterEntry() {
        this.set(true);
    }

    public boolean isEnterEntry() {
        Boolean status = this.get();
        return status != null && status;
    }
}
