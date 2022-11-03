package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Set;

public abstract class TaintFlowNode extends PolicyNode {
    protected Set<TaintPosition> targets;

    public TaintFlowNode(Set<TaintPosition> targets, MethodMatcher methodMatcher) {
        super(methodMatcher);
        this.targets = targets;
    }

    public Set<TaintPosition> getTargets() {
        return this.targets;
    }

    public void setTargets(Set<TaintPosition> sources) {
        this.targets = sources;
    }
}
