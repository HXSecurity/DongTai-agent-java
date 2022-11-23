package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintCommand;

import java.util.Set;

public class PropagatorNode extends TaintFlowNode {
    private final PolicyNodeType type = PolicyNodeType.PROPAGATOR;
    private Set<TaintPosition> sources;
    private TaintCommand command;
    private String[] commandArguments;

    public PropagatorNode(Set<TaintPosition> sources, Set<TaintPosition> targets,
                          TaintCommand command, String[] commandArguments, MethodMatcher methodMatcher) {
        super(targets, methodMatcher);
        this.sources = sources;
        this.command = command;
        this.commandArguments = commandArguments;
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
}
