package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Set;

public class SourceNode extends TaintFlowNode {
    private Set<TaintPosition> sources;
    private String[] tags;

    public SourceNode(Set<TaintPosition> sources, Set<TaintPosition> targets, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
        this.sources = sources;
    }

    @Override
    public PolicyNodeType getType() {
        return PolicyNodeType.SOURCE;
    }

    public Set<TaintPosition> getSources() {
        return this.sources;
    }

    public void setSources(Set<TaintPosition> sources) {
        this.sources = sources;
    }

    public String[] getTags() {
        return this.tags;
    }

    public boolean hasTags() {
        return this.tags != null && this.tags.length > 0;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
