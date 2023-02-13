package io.dongtai.iast.common.scope;

public class ScopeAggregator {
    private final GeneralScope httpRequestScope = new GeneralScope();
    private final GeneralScope httpEntryScope = new GeneralScope();
    private final GeneralScope servletInputStreamReadScope = new GeneralScope();
    private final PolicyScope policyScope = new PolicyScope();

    public GeneralScope getHttpRequestScope() {
        return httpRequestScope;
    }

    public GeneralScope getHttpEntryScope() {
        return httpEntryScope;
    }

    public GeneralScope getServletInputStreamReadScope() {
        return servletInputStreamReadScope;
    }

    public PolicyScope getPolicyScope() {
        return policyScope;
    }
}
