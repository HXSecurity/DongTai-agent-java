package io.dongtai.iast.common.scope;

public class ScopeAggregator {
    private final GeneralScope httpRequestScope = new GeneralScope();
    private final GeneralScope httpEntryScope = new GeneralScope();
    private final GeneralScope httpResponseHeaderScope = new GeneralScope();
    private final PolicyScope policyScope = new PolicyScope();

    public GeneralScope getHttpRequestScope() {
        return httpRequestScope;
    }

    public GeneralScope getHttpEntryScope() {
        return httpEntryScope;
    }

    public GeneralScope getHttpResponseHeaderScope() {
        return httpResponseHeaderScope;
    }

    public PolicyScope getPolicyScope() {
        return policyScope;
    }
}
