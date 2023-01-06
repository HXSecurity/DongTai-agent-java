package io.dongtai.plugin;

import io.grpc.*;

import java.lang.dongtai.TraceIdHandler;
import java.util.HashMap;
import java.util.Map;

public class GrpcProxy {
    private static Map<String, Object> metadata;

    public static Object interceptChannel(Object channel, Object traceIdHandler) {
        try {
            Channel interceptedChannel = (Channel) channel;
            TraceIdHandler h = (TraceIdHandler) traceIdHandler;
            return ClientInterceptors.intercept(interceptedChannel, new DongTaiClientInterceptor(h));
        } catch (Throwable e) {
            // fixme: remove throw exception
            e.printStackTrace();
        }
        return channel;
    }

    public static Object interceptService(Object service) {
        try {
            metadata = null;
            metadata = new HashMap<String, Object>(32);
            ServerServiceDefinition interceptedService = (ServerServiceDefinition) service;
            return ServerInterceptors.intercept(interceptedService, new DongTaiServerInterceptor());
        } catch (Throwable e) {
            // fixme: remove throw exception
            e.printStackTrace();
        }
        return service;
    }

    public static Map<String, Object> getServerMeta() {
        return metadata;
    }

    public static void addMetaItem(String key, Object obj) {
        if (metadata != null) {
            metadata.put(key, obj);
        }
    }
}
