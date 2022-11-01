package io.dongtai.iast.core.scope;

public class ScopeAggregator {
    private final GeneralScope httpRequestScope = new GeneralScope();
    private final GeneralScope httpEntryScope = new GeneralScope();
    private final PolicyScope policyScope = new PolicyScope();


    public GeneralScope getHttpRequestScope() {
        return httpRequestScope;
    }

    public GeneralScope getHttpEntryScope() {
        return httpEntryScope;
    }

    public PolicyScope getPolicyScope() {
        return policyScope;
    }
}
