package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe;

public interface XXEChecker {
    void setSourceObjectAndParameters(Object sourceObject, Object[] sourceParameters);

    Object getSourceObject();

    boolean match(Object obj);

    Support getSupport(Object obj);
}
