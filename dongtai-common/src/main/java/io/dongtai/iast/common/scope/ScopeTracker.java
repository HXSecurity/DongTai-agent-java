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
            case SERVLET_INPUT_STREAM_READ:
                return this.get().getServletInputStreamReadScope();
            case SERVLET_OUTPUT_STREAM_WRITE:
                return this.get().getServletOutputStreamWriteScope();
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
