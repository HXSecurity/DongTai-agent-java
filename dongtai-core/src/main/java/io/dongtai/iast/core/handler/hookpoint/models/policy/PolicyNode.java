package io.dongtai.iast.core.handler.hookpoint.models.policy;

public abstract class PolicyNode {
    private String hashString;
    private Inheritable inheritable;
    private boolean ignoreInternal;
    private boolean ignoreBlacklist;

    protected MethodMatcher methodMatcher;

    public PolicyNode(MethodMatcher methodMatcher) {
        this.methodMatcher = methodMatcher;
    }

    public abstract PolicyNodeType getType();

    public Inheritable getInheritable() {
        return this.inheritable;
    }

    public void setInheritable(Inheritable inheritable) {
        this.inheritable = inheritable;
    }

    public boolean isIgnoreInternal() {
        return this.ignoreInternal;
    }

    public void setIgnoreInternal(boolean ignoreInternal) {
        this.ignoreInternal = ignoreInternal;
    }

    public boolean isIgnoreBlacklist() {
        return this.ignoreBlacklist;
    }

    public void setIgnoreBlacklist(boolean ignoreBlacklist) {
        this.ignoreBlacklist = ignoreBlacklist;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    @Override
    public String toString() {
        if (this.hashString == null) {
            this.hashString = getType().getName() + "/" + this.methodMatcher.toString();
        }
        return this.hashString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() == obj.getClass()) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
