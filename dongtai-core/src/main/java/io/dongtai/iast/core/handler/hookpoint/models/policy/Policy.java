package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.*;

public class Policy {
    private final List<SourceNode> sources = new ArrayList<SourceNode>();
    private final List<PropagatorNode> propagators = new ArrayList<PropagatorNode>();
    private final List<SinkNode> sinks = new ArrayList<SinkNode>();
    private final Map<String, PolicyNode> policyNodesMap = new HashMap<String, PolicyNode>();
    private final Set<String> classHooks = new HashSet<String>();
    private final Set<String> ancestorClassHooks = new HashSet<String>();

    public List<SourceNode> getSources() {
        return sources;
    }

    public void addSource(SourceNode source) {
        this.sources.add(source);
        addHooks(source);
    }

    public List<PropagatorNode> getPropagators() {
        return propagators;
    }

    public void addPropagator(PropagatorNode propagator) {
        this.propagators.add(propagator);
        addHooks(propagator);
    }

    public List<SinkNode> getSinks() {
        return sinks;
    }

    public void addSink(SinkNode sink) {
        this.sinks.add(sink);
        addHooks(sink);
    }

    public PolicyNode getPolicyNode(String methodMatcher) {
        return this.policyNodesMap.get(methodMatcher);
    }

    public Map<String, PolicyNode> getPolicyNodesMap() {
        return this.policyNodesMap;
    }

    public void addHooks(PolicyNode node) {
        SignatureMethodMatcher methodMatcher;
        if (node.getMethodMatcher() instanceof SignatureMethodMatcher) {
            methodMatcher = (SignatureMethodMatcher) node.getMethodMatcher();
            this.policyNodesMap.put(methodMatcher.toString(), node);
            addHooks(methodMatcher.getSignature().getClassName(), node.getInheritable());
        }
    }

    public void addHooks(String className, Inheritable inheritable) {
        if (Inheritable.ALL.equals(inheritable) || Inheritable.SELF.equals(inheritable)) {
            this.classHooks.add(className);
        }
        if (Inheritable.ALL.equals(inheritable) || Inheritable.SUBCLASS.equals(inheritable)) {
            this.ancestorClassHooks.add(className);
        }
    }

    public String getMatchedClass(String className, Set<String> ancestors) {
        if (this.classHooks.contains(className)) {
            return className;
        }
        for (String ancestor : ancestors) {
            if (this.ancestorClassHooks.contains(ancestor)) {
                return ancestor;
            }
        }
        return null;
    }

    public boolean isMatchClass(String className) {
        return this.classHooks.contains(className) || this.ancestorClassHooks.contains(className);
    }
}
