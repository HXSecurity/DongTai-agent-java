package com.secnium.iast.core.threadlocalpool;

import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RequestContext extends ThreadLocal<Map<String, Object>> {
    @Override
    protected Map<String, Object> initialValue() {
        return null;
    }

    public String getCookieValue() {
        return (String) this.get().get("cookies");
    }
}
