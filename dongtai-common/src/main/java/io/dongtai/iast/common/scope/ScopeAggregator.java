package io.dongtai.iast.common.scope;

public class ScopeAggregator {
    private final GeneralScope httpRequestScope = new GeneralScope();
    private final GeneralScope httpEntryScope = new GeneralScope();
    private final GeneralScope dubboRequestScope = new GeneralScope();
    private final GeneralScope dubboEntryScope = new GeneralScope();
    private final GeneralScope dubboSourceScope = new GeneralScope();
    private final GeneralScope servletInputStreamReadScope = new GeneralScope();
    private final GeneralScope servletOutputStreamWriteScope = new GeneralScope();
    private final PolicyScope policyScope = new PolicyScope();

    public GeneralScope getHttpRequestScope() {
        return httpRequestScope;
    }

    public GeneralScope getHttpEntryScope() {
        return httpEntryScope;
    }

    public GeneralScope getDubboRequestScope() {
        return dubboRequestScope;
    }

    public GeneralScope getDubboEntryScope() {
        return dubboEntryScope;
    }

    public GeneralScope getDubboSourceScope() {
        return dubboSourceScope;
    }

    public GeneralScope getServletInputStreamReadScope() {
        return servletInputStreamReadScope;
    }

    public GeneralScope getServletOutputStreamWriteScope() {
        return servletOutputStreamWriteScope;
    }

    public PolicyScope getPolicyScope() {
        return policyScope;
    }
}
