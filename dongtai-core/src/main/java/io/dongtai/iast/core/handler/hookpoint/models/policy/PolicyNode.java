package io.dongtai.iast.core.handler.hookpoint.models.policy;

public abstract class PolicyNode {
    private String hashString;
    private Inheritable inheritable;

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

    public void setInheritable(String inheritable) {
        this.inheritable = Inheritable.parse(inheritable);
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
