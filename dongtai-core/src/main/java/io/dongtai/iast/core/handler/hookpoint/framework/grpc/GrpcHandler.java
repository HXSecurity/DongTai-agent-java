package io.dongtai.iast.core.handler.hookpoint.framework.grpc;

import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class GrpcHandler {
    private static Class<?> classOfGrpcProxy;
    private static IastClassLoader gRpcClassLoader;
    public static File grpcPluginPath;
    private static Method methodOfInterceptChannel;
    private static Method methodOfInterceptService;

    static {
        grpcPluginPath = new File(System.getProperty("java.io.tmpdir") + File.separator + "iast" + File.separator + "dongtai-grpc.jar");
        if (!grpcPluginPath.exists()) {
            HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-grpc", grpcPluginPath.getAbsolutePath());
        }
    }

    private static void createClassLoader(Object channel) {
        try {
            if (gRpcClassLoader != null) {
                return;
            }
            gRpcClassLoader = new IastClassLoader(
                    channel.getClass().getClassLoader(),
                    new URL[]{grpcPluginPath.toURI().toURL()}
            );

            classOfGrpcProxy = gRpcClassLoader.loadClass("io.dongtai.plugin.GrpcProxy");
            methodOfInterceptChannel = classOfGrpcProxy
                    .getDeclaredMethod("interceptChannel", Object.class, String.class, String.class);
            methodOfInterceptService = classOfGrpcProxy
                    .getDeclaredMethod("interceptService", Object.class);
            grpcPluginPath.delete();
        } catch (MalformedURLException | NoSuchMethodException e) {
            DongTaiLog.error(e);
        }
    }

    public static Object interceptChannel(Object channel) {
        if (methodOfInterceptChannel == null) {
            createClassLoader(channel);
        }
        try {
            return methodOfInterceptChannel.invoke(null, channel, ContextManager.getHeaderKey(), ContextManager.getSegmentId());
        } catch (Exception e) {
            DongTaiLog.error(e);
        }

        return channel;
    }

    public static Object interceptService(Object service) {
        try {
            if (methodOfInterceptService == null) {
                createClassLoader(service);
            }
            // todo: 获取 ContextManager、
            return methodOfInterceptService.invoke(null, service);
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
        return service;
    }
}
