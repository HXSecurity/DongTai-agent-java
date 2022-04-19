package io.dongtai.plugin;

import io.grpc.*;

import java.lang.dongtai.TraceIdHandler;

public class DongTaiClientInterceptor implements ClientInterceptor {
    private TraceIdHandler traceIdHandler;

    public DongTaiClientInterceptor(TraceIdHandler traceIdHandler) {
        this.traceIdHandler = traceIdHandler;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        String methodName = method.getFullMethodName();
        String methodType = method.getType().toString();
        String target = channel.toString();
        return new DongTaiClientCall<ReqT, RespT>(channel.newCall(method, callOptions), methodName, methodType, target, traceIdHandler);
    }
}
