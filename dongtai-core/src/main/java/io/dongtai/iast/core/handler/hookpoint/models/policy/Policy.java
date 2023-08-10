package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;

import java.util.*;

public class Policy {
    private final List<SourceNode> sources = new ArrayList<SourceNode>();
    private final List<PropagatorNode> propagators = new ArrayList<PropagatorNode>();
    private final List<SinkNode> sinks = new ArrayList<SinkNode>();
    private final List<ValidatorNode> validators = new ArrayList<ValidatorNode>();
    private final Map<String, PolicyNode> policyNodesMap = new HashMap<String, PolicyNode>();
    private final Set<String> classHooks = new HashSet<String>();
    private final Set<String> ancestorClassHooks = new HashSet<String>();

    private final Set<String> blacklistHooks = new HashSet<String>();
    private final Set<String> ignoreInternalHooks = new HashSet<String>();
    private final Set<String> ignoreBlacklistHooks = new HashSet<String>();

    public List<SourceNode> getSources() {
        return sources;
    }

    public void addSource(SourceNode source) {
        this.sources.add(source);
        addPolicyNode(source);
    }

    public List<PropagatorNode> getPropagators() {
        return propagators;
    }

    public void addPropagator(PropagatorNode propagator) {
        this.propagators.add(propagator);
        addPolicyNode(propagator);
    }

    public List<SinkNode> getSinks() {
        return sinks;
    }

    public void addSink(SinkNode sink) {
        this.sinks.add(sink);
        addPolicyNode(sink);
    }

    public void addValidator(ValidatorNode validator) {
        this.validators.add(validator);
        addPolicyNode(validator);
    }

    public PolicyNode getPolicyNode(String policyKey) {
        return this.policyNodesMap.get(policyKey);
    }

    public Map<String, PolicyNode> getPolicyNodesMap() {
        return this.policyNodesMap;
    }

    public void addPolicyNode(PolicyNode node) {
        SignatureMethodMatcher methodMatcher;
        if (node.getMethodMatcher() instanceof SignatureMethodMatcher) {
            methodMatcher = (SignatureMethodMatcher) node.getMethodMatcher();
            this.policyNodesMap.put(node.toString(), node);
            addHooks(methodMatcher.getSignature().getClassName(), node.getInheritable());
            if (node.isIgnoreInternal()) {
                this.ignoreInternalHooks.add(methodMatcher.getSignature().getClassName());
            }
            if (node.isIgnoreBlacklist()) {
                this.ignoreBlacklistHooks.add(methodMatcher.getSignature().getClassName());
            }
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

    public Set<String> getMatchedClass(ClassContext classContext, String className, Set<String> ancestors) {
        Set<String> matchedClassSet = new HashSet<>();
        if (this.classHooks.contains(className)) {
            classContext.setMatchedClassName(className);
            matchedClassSet.add(className);
            return matchedClassSet;
        }
        for (String ancestor : ancestors) {
            if (this.ancestorClassHooks.contains(ancestor)) {
                matchedClassSet.add(ancestor);
            }
        }
        return matchedClassSet;
    }

    public boolean isMatchClass(String className) {
        return this.classHooks.contains(className) || this.ancestorClassHooks.contains(className);
    }

    public Set<String> getClassHooks() {
        return this.classHooks;
    }

    public Set<String> getAncestorClassHooks() {
        return this.ancestorClassHooks;
    }

    public void addBlacklistHooks(String className) {
        this.blacklistHooks.add(className);
    }

    public boolean isBlacklistHooks(String className) {
        return this.blacklistHooks.contains(className);
    }

    public boolean isIgnoreInternalHooks(String className) {
        return this.ignoreInternalHooks.contains(className);
    }

    public boolean isIgnoreBlacklistHooks(String className) {
        return this.ignoreBlacklistHooks.contains(className);
    }
}
