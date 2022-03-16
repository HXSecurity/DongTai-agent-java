package io.dongtai.iast.core.handler.hookpoint.framework.grpc;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.graphy.GraphBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GrpcHandler {
    private static IastClassLoader gRpcClassLoader;
    public static File grpcPluginPath;
    private static Method methodOfInterceptChannel;
    private static Method methodOfInterceptService;
    private static Method methodOfGetRequestMetadata;

    static {
        grpcPluginPath = new File(System.getProperty("java.io.tmpdir") + File.separator + "iast" + File.separator + "dongtai-grpc.jar");
        if (!grpcPluginPath.exists()) {
            HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-grpc", grpcPluginPath.getAbsolutePath());
        }
    }

    /**
     * 根据 channel 或 service 创建继承自 grpc classloader 的自定义类加载器，后续创建拦截器用于获取相关数据
     *
     * @param channel
     */
    private static void createClassLoader(Object channel) {
        try {
            if (gRpcClassLoader != null) {
                return;
            }
            gRpcClassLoader = new IastClassLoader(
                    channel.getClass().getClassLoader(),
                    new URL[]{grpcPluginPath.toURI().toURL()}
            );

            Class<?> classOfGrpcProxy = gRpcClassLoader.loadClass("io.dongtai.plugin.GrpcProxy");
            methodOfInterceptChannel = classOfGrpcProxy
                    .getDeclaredMethod("interceptChannel", Object.class, String.class, String.class);
            methodOfInterceptService = classOfGrpcProxy
                    .getDeclaredMethod("interceptService", Object.class);
            methodOfGetRequestMetadata = classOfGrpcProxy.getDeclaredMethod("getServerMeta");
            grpcPluginPath.delete();
        } catch (MalformedURLException | NoSuchMethodException e) {
            DongTaiLog.error(e);
        }
    }

    /**
     * 拦截 Grpc client 的 channel，后续client调用Server端服务会经过该拦截器
     *
     * @param channel
     * @return
     */
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

    /**
     * 拦截 Grpc 的 Service，后续服务的调用会经过该拦截器
     *
     * @param service
     * @return
     */
    public static Object interceptService(Object service) {
        try {
            if (methodOfInterceptService == null) {
                createClassLoader(service);
            }
            return methodOfInterceptService.invoke(null, service);
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
        return service;
    }

    /**
     * 进入 Grpc 处理逻辑，获取请求元数据，初始化 agent 状态
     * todo: 细化元数据，保证采集到的数据可人工查看
     */
    public static void startTrace() {
        try {
            Map<String, Object> metadata = (Map<String, Object>) methodOfGetRequestMetadata.invoke(null);
            if (metadata.containsKey("dt-traceid")) {
                ContextManager.getOrCreateGlobalTraceId((String) metadata.get("dt-traceid"), EngineManager.getAgentId());
            } else {
                String newTraceId = ContextManager.getOrCreateGlobalTraceId(null, EngineManager.getAgentId());
                String spanId = ContextManager.getSpanId(newTraceId, EngineManager.getAgentId());
                metadata.put("dt-traceid", newTraceId);
                metadata.put("dt-spandid", spanId);
            }
            EngineManager.REQUEST_CONTEXT.set(metadata);
            EngineManager.TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
            EngineManager.TAINT_POOL.set(new HashSet<Object>());
            EngineManager.TAINT_HASH_CODES.set(new HashSet<Integer>());
            EngineManager.SCOPE_TRACKER.get().enterGrpc();
        } catch (Exception e) {
            DongTaiLog.error(e);
        }
    }

    /**
     * 关闭Grpc的Call调用，上报数据到Server端并清空agent状态
     */
    public static void closeGrpcCall() {
        try {
            if (EngineManager.isEnterEntry()) {
                EngineManager.turnOffDongTai();
                EngineManager.SCOPE_TRACKER.get().leaveGrpc();
                EngineManager.maintainRequestCount();
                GraphBuilder.buildAndReport(null, null);
                EngineManager.cleanThreadState();
                EngineManager.turnOnDongTai();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

}
