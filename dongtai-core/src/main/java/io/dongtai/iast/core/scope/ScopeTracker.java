package io.dongtai.iast.core.scope;

public class ScopeTracker extends ThreadLocal<ScopeAggregator> {
    @Override
    protected ScopeAggregator initialValue() {
        return new ScopeAggregator();
    }

    public GeneralScope getHttpRequestScope() {
        return this.get().getHttpRequestScope();
    }

    public GeneralScope getHttpEntryScope() {
        return this.get().getHttpEntryScope();
    }

    public boolean isEnterEntry() {
        return this.get().getHttpEntryScope().in();
    }

    public PolicyScope getPolicyScope() {
        return this.get().getPolicyScope();
    }
}
