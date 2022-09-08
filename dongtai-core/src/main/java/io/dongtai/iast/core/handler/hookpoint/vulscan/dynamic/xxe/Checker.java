package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

public interface Checker {
    Object getCheckObject(MethodEvent event);

    boolean match(Object obj);

    Support getSupport(Object obj);
}
