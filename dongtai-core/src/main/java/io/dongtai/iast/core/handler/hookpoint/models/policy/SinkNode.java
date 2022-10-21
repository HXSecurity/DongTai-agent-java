package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Set;

public class SinkNode extends PolicyNode {
    private final PolicyNodeType type = PolicyNodeType.SINK;
    private Set<TaintPosition> sources;

    public SinkNode(Set<TaintPosition> sources, MethodMatcher methodMatcher) {
        super(methodMatcher);
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
