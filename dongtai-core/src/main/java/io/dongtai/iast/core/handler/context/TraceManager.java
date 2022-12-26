package io.dongtai.iast.core.handler.context;

import java.lang.dongtai.TraceIdHandler;

public class TraceManager implements TraceIdHandler {
    @Override
    public String getTraceKey() {
        return ContextManager.getHeaderKey();
    }

    @Override
    public String getTraceId() {
        return ContextManager.currentTraceId();
    }
}
