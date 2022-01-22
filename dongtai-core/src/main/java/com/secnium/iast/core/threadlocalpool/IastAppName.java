package com.secnium.iast.core.threadlocalpool;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastAppName extends ThreadLocal<String> {
    public static final String DEFAULT_APP_NAME = "";

    @Override
    protected String initialValue() {
        return null;
    }

    @Override
    public String get() {
        String appName = super.get();
        return appName == null ? DEFAULT_APP_NAME : appName;
    }

}
