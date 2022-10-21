package io.dongtai.iast.core.handler.hookpoint.models.policy;

public interface MethodMatcher {
    boolean match(MethodModel method);
}
