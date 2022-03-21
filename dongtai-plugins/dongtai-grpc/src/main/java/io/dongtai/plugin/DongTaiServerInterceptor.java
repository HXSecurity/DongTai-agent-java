package io.dongtai.plugin;

import io.grpc.*;

import java.util.Set;

public class DongTaiServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Set<String> keys = metadata.keys();
        for (String key : keys) {
            Metadata.Key<?> metaItemKey;
            if (key.endsWith("-bin")) {
                continue;
            }
            metaItemKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
            GrpcProxy.addMetaItem(key, metadata.get(metaItemKey));
        }
        GrpcProxy.addMetaItem("requestURI", serverCall.getMethodDescriptor().getFullMethodName());
        GrpcProxy.addMetaItem("serverAddr", serverCall.getAuthority());
        return new DongTaiServerCallListener<ReqT>(serverCallHandler.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
            @Override
            public void close(Status status, Metadata trailers) {
                super.close(status, trailers);
            }
        }, metadata));
    }
}
