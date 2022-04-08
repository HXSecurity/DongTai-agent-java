package io.dongtai.plugin;

import io.grpc.*;

public class DongTaiClientInterceptor implements ClientInterceptor {
    private String traceId;
    private String traceKey;

    public DongTaiClientInterceptor(String traceKey, String traceId) {
        this.traceId = traceId;
        this.traceKey = traceKey;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        String methodName = method.getFullMethodName();
        String methodType = method.getType().toString();
        String target = channel.toString();
        return new DongTaiClientCall<ReqT, RespT>(channel.newCall(method, callOptions), methodName, methodType, target, traceKey, traceId);
    }
}
