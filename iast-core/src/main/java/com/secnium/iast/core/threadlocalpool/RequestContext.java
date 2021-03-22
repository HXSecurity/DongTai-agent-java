package com.secnium.iast.core.threadlocalpool;

import com.secnium.iast.core.util.http.HttpRequest;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RequestContext extends ThreadLocal<HttpRequest> {
    @Override
    protected HttpRequest initialValue() {
        return null;
    }

    public String getCookieValue() {
        return this.get().getCookieValue();
    }
}
