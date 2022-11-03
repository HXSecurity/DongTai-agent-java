package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;

public interface MethodMatcher {
    boolean match(MethodContext method);
}
