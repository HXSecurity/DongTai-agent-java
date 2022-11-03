package io.dongtai.iast.common.scope;

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

    public boolean inEnterEntry() {
        return this.get().getHttpEntryScope().in();
    }

    public PolicyScope getPolicyScope() {
        return this.get().getPolicyScope();
    }

    public boolean inAgent() {
        return this.get().getPolicyScope().inAgent();
    }
}
