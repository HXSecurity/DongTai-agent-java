package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Set;

public class SourceNode extends TaintFlowNode {
    private final PolicyNodeType type = PolicyNodeType.SOURCE;

    private Set<TaintPosition> sources;

    public SourceNode(Set<TaintPosition> sources, Set<TaintPosition> targets, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
        this.sources = sources;
    }

    public PolicyNodeType getType() {
        return this.type;
    }

    public Set<TaintPosition> getSources() {
        return this.sources;
    }

    public void setSources(Set<TaintPosition> sources) {
        this.sources = sources;
    }
}
