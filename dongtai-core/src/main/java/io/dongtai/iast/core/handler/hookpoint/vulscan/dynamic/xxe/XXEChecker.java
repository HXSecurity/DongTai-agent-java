package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

import java.util.List;

public interface XXEChecker {
    void setSourceObjectAndParameters(Object sourceObject, Object[] sourceParameters);

    List<Object> getCheckObjects();

    boolean match(Object obj);

    Support getSupport(Object obj);
}
