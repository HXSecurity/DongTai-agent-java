package io.dongtai.iast.core.handler.hookpoint.framework.grpc;

import io.dongtai.iast.core.handler.context.TraceManager;

public class GrpcTraceManager extends TraceManager {
    @Override
    public String getTraceId() {
        String traceId = super.getTraceId();
        GrpcHandler.setSharedTraceId(traceId);
        return traceId;
    }
}
