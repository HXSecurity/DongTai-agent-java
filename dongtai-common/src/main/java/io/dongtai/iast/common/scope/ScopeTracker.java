package io.dongtai.iast.common.scope;

public class ScopeTracker extends ThreadLocal<ScopeAggregator> {
    @Override
    protected ScopeAggregator initialValue() {
        return new ScopeAggregator();
    }

    public GeneralScope getScope(Scope scope) {
        switch (scope) {
            case HTTP_REQUEST:
                return this.get().getHttpRequestScope();
            case HTTP_ENTRY:
                return this.get().getHttpEntryScope();
            case HTTP_RESPONSE_HEADER:
                return this.get().getHttpResponseHeaderScope();
            default:
                return null;
        }
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
