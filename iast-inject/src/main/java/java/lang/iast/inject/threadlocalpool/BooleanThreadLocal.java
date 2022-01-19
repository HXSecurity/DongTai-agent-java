package java.lang.iast.inject.threadlocalpool;

public class BooleanThreadLocal extends ThreadLocal<Boolean>{

    boolean defaultValue;

    public BooleanThreadLocal(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    protected Boolean initialValue() {
        return null;
    }

    public void setFalse(){
        this.set(true);
    }

    public void enterEntry() {
        this.set(true);
    }

    public boolean isEnterEntry() {
        Boolean status = this.get();
        return status != null && status;
    }
}
