package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommandRunner;

import java.util.Set;

public class PropagatorNode extends TaintFlowNode {
    private Set<TaintPosition> sources;
    private TaintCommandRunner commandRunner;
    private String[] tags;
    private String[] untags;

    public PropagatorNode(Set<TaintPosition> sources, Set<TaintPosition> targets, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
        this.sources = sources;
    }

    @Override
    public PolicyNodeType getType() {
        return PolicyNodeType.PROPAGATOR;
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

    public String[] getUntags() {
        return this.untags;
    }

    public void setUntags(String[] untags) {
        this.untags = untags;
    }

    public TaintCommandRunner getCommandRunner() {
        return this.commandRunner;
    }

    public void setCommandRunner(TaintCommandRunner r) {
        this.commandRunner = r;
    }
}
