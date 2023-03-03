package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommand;

import java.util.Set;

public class PropagatorNode extends TaintFlowNode {
    private Set<TaintPosition> sources;
    private TaintCommand command;
    private String[] commandArguments;
    private String[] tags;
    private String[] untags;

    public PropagatorNode(Set<TaintPosition> sources, Set<TaintPosition> targets,
                          TaintCommand command, String[] commandArguments, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
        this.sources = sources;
        this.command = command;
        this.commandArguments = commandArguments;
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

    public TaintCommand getCommand() {
        return this.command;
    }

    public void setCommand(TaintCommand command) {
        this.command = command;
    }

    public String[] getCommandArguments() {
        return this.commandArguments;
    }

    public void setCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
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
}
