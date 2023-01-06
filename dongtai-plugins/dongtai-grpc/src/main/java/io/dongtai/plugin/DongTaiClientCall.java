package io.dongtai.plugin;

import io.grpc.*;

import java.lang.dongtai.TraceIdHandler;

public class DongTaiClientCall<REQUEST, RESPONSE> extends ForwardingClientCall.SimpleForwardingClientCall<REQUEST, RESPONSE> {
    String serviceName;
    String serviceType;
    String targetService;
    String pluginName;
    String traceKey;
    String traceId;

    protected DongTaiClientCall(ClientCall<REQUEST, RESPONSE> delegate, String serviceName, String serviceType, String targetService, TraceIdHandler traceIdHandler) {
        super(delegate);
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.targetService = targetService;
        this.pluginName = "GRPC";
        this.traceKey = traceIdHandler.getTraceKey();
        this.traceId = traceIdHandler.getTraceId();
    }

    @Override
    public void start(Listener<RESPONSE> responseListener, Metadata headers) {
        try {
            Metadata.Key<String> dtTraceId = Metadata.Key.of(traceKey, Metadata.ASCII_STRING_MARSHALLER);
            headers.discardAll(dtTraceId);
            headers.put(dtTraceId, traceId);
        } catch (Throwable e) {
            // fixme: solve exception
        }
        super.start(responseListener, headers);
    }
}
