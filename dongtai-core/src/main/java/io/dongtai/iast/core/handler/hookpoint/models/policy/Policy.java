package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.*;

public class Policy {
    private List<SourceNode> sources = new ArrayList<SourceNode>();
    private List<PropagatorNode> propagators = new ArrayList<PropagatorNode>();
    private List<SinkNode> sinks = new ArrayList<SinkNode>();
    private Set<String> hookClasses = new HashSet<String>();

    public List<SourceNode> getSources() {
        return sources;
    }

    public void addSource(SourceNode source) {
        this.sources.add(source);
        addHookClasses(source);
    }

    public List<PropagatorNode> getPropagators() {
        return propagators;
    }

    public void addPropagator(PropagatorNode propagator) {
        this.propagators.add(propagator);
        addHookClasses(propagator);
    }

    public List<SinkNode> getSinks() {
        return sinks;
    }

    public void addSink(SinkNode sink) {
        this.sinks.add(sink);
        addHookClasses(sink);
    }

    public void addHookClasses(PolicyNode node) {
        if (node.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.hookClasses.add(((SignatureMethodMatcher) node.getMethodMatcher()).getSignature().getClassName());
        }
    }

    public Set<String> getHookClasses() {
        return this.hookClasses;
    }
}
