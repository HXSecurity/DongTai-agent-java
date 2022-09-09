package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

public interface XXEChecker {
    void setMethodEvent(MethodEvent event);

    Object getCheckObject();

    boolean match(Object obj);

    Support getSupport(Object obj);
}
