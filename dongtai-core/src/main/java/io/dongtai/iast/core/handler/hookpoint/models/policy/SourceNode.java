package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Set;

public class SourceNode extends TaintFlowNode {
    private final PolicyNodeType type = PolicyNodeType.SOURCE;

    public SourceNode(Set<TaintPosition> targets, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
    }

    public PolicyNodeType getType() {
        return this.type;
    }
}
