package io.dongtai.iast.core;

import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastServer;
import io.dongtai.iast.core.handler.hookpoint.models.KrpcApiModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.threadlocal.BooleanThreadLocal;
import io.dongtai.iast.core.utils.threadlocal.IastScopeTracker;
import io.dongtai.iast.core.utils.threadlocal.IastServerPort;
import io.dongtai.iast.core.utils.threadlocal.IastTaintHashCodes;
import io.dongtai.iast.core.utils.threadlocal.IastTaintPool;
import io.dongtai.iast.core.utils.threadlocal.IastTrackMap;
import io.dongtai.iast.core.utils.threadlocal.RequestContext;
import io.dongtai.iast.core.service.ServiceFactory;
import io.dongtai.iast.core.utils.PropertyUtils;

import java.util.*;

/**
 * 存储全局信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EngineManager {

    private static EngineManager instance;
    private final int agentId;
    private final boolean supportLazyHook;
    private final boolean saveBytecode;

    private static final BooleanThreadLocal ENTER_HTTP_ENTRYPOINT = new BooleanThreadLocal(false);
    public static final RequestContext REQUEST_CONTEXT = new RequestContext();
    public static final RequestContext RESPONSE_CONTEXT = new RequestContext();
    public static final IastTrackMap TRACK_MAP = new IastTrackMap();
    public static final IastTaintPool TAINT_POOL = new IastTaintPool();
    public static final IastTaintHashCodes TAINT_HASH_CODES = new IastTaintHashCodes();
    public static final IastScopeTracker SCOPE_TRACKER = new IastScopeTracker();
    private static final IastServerPort LOGIN_LOGIC_WEIGHT = new IastServerPort();
    public static Map<String, KrpcApiModel> KRPC_API_SITEMAP = new HashMap<>();
    public static Boolean KRPC_API_SITEMAP_IS_SEND = false;

    /**
     * 标记是否位于 IAST 的代码中，如果该值为 true 表示 iast 在运行中；如果 该值为 false 表示当前位置在iast的代码中，所有iast逻辑都bypass，直接退出
     */
    private static final BooleanThreadLocal DONGTAI_STATE = new BooleanThreadLocal(false);
    public static IastServer SERVER;

    private static boolean logined = false;
    private static int reqCounts = 0;
    private static int enableDongTai = 0;

    public static void turnOnDongTai() {
        DONGTAI_STATE.set(true);
    }

    public static void turnOffDongTai() {
        DONGTAI_STATE.set(false);
    }

    /**
     * Determine whether the current code flow enters the engine processing logic
     *
     * @return
     */
    public static Boolean isDongTaiRunning() {
        Boolean status = DONGTAI_STATE.get();
        return status != null && status;
    }

    public static EngineManager getInstance() {
        return instance;
    }

    public static EngineManager getInstance(int agentId) {
        if (instance == null) {
            instance = new EngineManager(agentId);
        }
        return instance;
    }

    public static void setInstance() {
        instance = null;
    }

    private EngineManager(int agentId) {
        PropertyUtils cfg = PropertyUtils.getInstance();
        this.supportLazyHook = cfg.isEnableAllHook();
        this.saveBytecode = cfg.isEnableDumpClass();
        this.agentId = agentId;

    }

    /**
     * 清除当前线程的状态，避免线程重用导致的ThreadLocal产生内存泄漏的问题
     */
    public static void cleanThreadState() {
        EngineManager.LOGIN_LOGIC_WEIGHT.remove();
        EngineManager.ENTER_HTTP_ENTRYPOINT.remove();
        EngineManager.REQUEST_CONTEXT.remove();
        EngineManager.RESPONSE_CONTEXT.remove();
        EngineManager.TRACK_MAP.remove();
        EngineManager.TAINT_POOL.remove();
        EngineManager.TAINT_HASH_CODES.remove();
        EngineManager.SCOPE_TRACKER.remove();
    }

    public static void maintainRequestCount() {
        EngineManager.reqCounts++;
    }

    /**
     * 获取引擎已检测的请求数量
     *
     * @return 产生的请求数量
     */
    public static int getRequestCount() {
        return EngineManager.reqCounts;
    }

    /**
     * 打开检测引擎
     */
    public static void turnOnEngine() {
        EngineManager.enableDongTai = 1;
    }

    /**
     * 关闭检测引擎
     */
    public static void turnOffEngine() {
        EngineManager.enableDongTai = 0;
    }

    /**
     * 检查灵芝引擎是否被开启
     *
     * @return true - 引擎已启动；false - 引擎未启动
     */
    public static boolean isEngineRunning() {
        return EngineManager.enableDongTai == 1;
    }

    public static void enterKrpcHttp() {
        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_POOL.set(new HashSet<Object>());
        TAINT_HASH_CODES.set(new HashSet<Integer>());
    }

    public boolean supportLazyHook() {
        return instance.supportLazyHook;
    }

    public static boolean getIsLoginLogic() {
        return LOGIN_LOGIC_WEIGHT.get() != null && LOGIN_LOGIC_WEIGHT.get().equals(2);
    }

    public static boolean getIsLoginLogicUrl() {
        return LOGIN_LOGIC_WEIGHT.get() != null && LOGIN_LOGIC_WEIGHT.get().equals(1);
    }

    public static void setIsLoginLogic() {
        if (LOGIN_LOGIC_WEIGHT.get() == null) {
            LOGIN_LOGIC_WEIGHT.set(1);
        } else {
            LOGIN_LOGIC_WEIGHT.set(LOGIN_LOGIC_WEIGHT.get() + 1);
        }
    }

    public static boolean isLogined() {
        return logined;
    }

    public static synchronized void setLogined() {
        logined = true;
    }

    public static boolean isTopLevelSink() {
        return SCOPE_TRACKER.isFirstLevelSink();
    }

    public static boolean hasTaintValue() {
        return SCOPE_TRACKER.hasTaintValue();
    }

    public boolean isEnableDumpClass() {
        return this.saveBytecode;
    }

    public static Integer getAgentId() {
        return instance.agentId;
    }

    public static void enterHttpEntry(Map<String, Object> requestMeta) {
        ServiceFactory.startService();
        if (null == SERVER) {
            // todo: read server addr and send to OpenAPI Service
            SERVER = new IastServer(
                    (String) requestMeta.get("serverName"),
                    (Integer) requestMeta.get("serverPort"),
                    true
            );
        }
        Map<String, String> headers = (Map<String, String>) requestMeta.get("headers");
        if (headers.containsKey("dt-traceid")) {
            ContextManager.getOrCreateGlobalTraceId(headers.get("dt-traceid"), EngineManager.getAgentId());
        } else {
            String newTraceId = ContextManager.getOrCreateGlobalTraceId(null, EngineManager.getAgentId());
            headers.put("dt-traceid", newTraceId);
        }
        ENTER_HTTP_ENTRYPOINT.enterEntry();
        REQUEST_CONTEXT.set(requestMeta);
        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_POOL.set(new HashSet<Object>());
        TAINT_HASH_CODES.set(new HashSet<Integer>());
    }

    public static void exitKrpcHttp(String host, String reqUrl, Map<String, String> reqHeader, String reqContent, String resContent, String text, String httpCode, String method, String queryString, String path, Map<String, String> resHeader) {
        if (null == SERVER) {
            SERVER = new IastServer(
                    host,
                    null,
                    true
            );
        }
        if (reqHeader.containsKey("dt-traceid")) {
            ContextManager.getOrCreateGlobalTraceId(reqHeader.get("dt-traceid"), EngineManager.getAgentId());
        } else {
            String newTraceId = ContextManager.getOrCreateGlobalTraceId(null, EngineManager.getAgentId());
            reqHeader.put("dt-traceid", newTraceId);
        }

        // todo: register server


        Map<String, Object> requestMeta = new HashMap<String, Object>(12);
        requestMeta.put("contextPath", path);
        requestMeta.put("servletPath", host);
        requestMeta.put("requestURL", reqUrl);
        requestMeta.put("requestURI", path);
        requestMeta.put("method", method);
        requestMeta.put("serverName", host);
        requestMeta.put("serverPort", "");
        requestMeta.put("queryString", queryString);
        requestMeta.put("protocol", text);
        requestMeta.put("scheme", "");
        requestMeta.put("remoteAddr", "");
        requestMeta.put("secure", "");
        requestMeta.put("body", reqContent);
        requestMeta.put("headers", reqHeader);
        Map<String, Object> responseMeta = new HashMap<String, Object>(2);
        String encodedHeader = getEncodedHeader(resHeader);
        String reqBody = text + " " + httpCode + "\n" + encodedHeader + "\n" +resContent;
        responseMeta.put("body",reqBody);
        responseMeta.put("headers","");

        REQUEST_CONTEXT.set(requestMeta);
        RESPONSE_CONTEXT.set(responseMeta);
    }

    public static String getEncodedHeader(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> headerItem : headers.entrySet()) {
            sb.append(headerItem.getKey());
            sb.append(":");
            sb.append(headerItem.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @param dubboService
     * @param attachments
     * @since 1.2.0
     */
    public static void enterDubboEntry(String dubboService, Map<String, String> attachments) {
        if (attachments != null) {
            if (attachments.containsKey(ContextManager.getHeaderKeyTraceId())) {
                ContextManager.getOrCreateGlobalTraceId(attachments.get(ContextManager.getHeaderKeyTraceId()),
                        EngineManager.getAgentId());
                ContextManager.addSpanId();
            } else {
                attachments.put(ContextManager.getHeaderKeyTraceId(), ContextManager.getSegmentId());
            }
        }
        if (ENTER_HTTP_ENTRYPOINT.isEnterEntry()) {
            return;
        }

        // todo: register server
        if (attachments != null) {
            Map<String, String> requestHeaders = new HashMap<String, String>(attachments.size());
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                requestHeaders.put(entry.getKey(), entry.getValue());
            }
            if (null == SERVER) {
                // todo: read server addr and send to OpenAPI Service
                SERVER = new IastServer(requestHeaders.get("dubbo"), 0, true);
            }
            Map<String, Object> requestMeta = new HashMap<String, Object>(12);
            requestMeta.put("protocol", "dubbo/" + requestHeaders.get("dubbo"));
            requestMeta.put("scheme", "dubbo");
            requestMeta.put("method", "RPC");
            requestMeta.put("secure", "true");
            requestMeta.put("requestURL", dubboService.split("\\?")[0]);
            requestMeta.put("requestURI", requestHeaders.get("path"));
            requestMeta.put("remoteAddr", "");
            requestMeta.put("queryString", "");
            requestMeta.put("headers", requestHeaders);
            requestMeta.put("body", "");
            requestMeta.put("contextPath", "");
            requestMeta.put("replay-request", false);

            REQUEST_CONTEXT.set(requestMeta);
        }

        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_POOL.set(new HashSet<Object>());
        TAINT_HASH_CODES.set(new HashSet<Integer>());
    }

    /**
     * @param krpcService
     * @param attachments
     * @since 1.3.2
     */
    public static void enterKrpcEntry(String krpcService, String uri, Map<String, String> attachments) {
        if (attachments != null) {
            if (attachments.containsKey(ContextManager.getHeaderKeyTraceId())) {
                ContextManager.getOrCreateGlobalTraceId(attachments.get(ContextManager.getHeaderKeyTraceId()),
                        EngineManager.getAgentId());
                ContextManager.addSpanId();
            } else {
                attachments.put(ContextManager.getHeaderKeyTraceId(), ContextManager.getOrCreateGlobalTraceId(null, EngineManager.getAgentId()));
            }
        }
        if (ENTER_HTTP_ENTRYPOINT.isEnterEntry()) {
            return;
        }

        // todo: register server
        if (attachments != null) {
            Map<String, String> requestHeaders = new HashMap<String, String>(attachments.size());
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                requestHeaders.put(entry.getKey(), entry.getValue());
            }
            if (null == SERVER) {
                // todo: read server addr and send to OpenAPI Service
                SERVER = new IastServer(requestHeaders.get("krpc"), 0, true);
            }
            Map<String, Object> requestMeta = new HashMap<String, Object>(12);
            requestMeta.put("protocol", "krpc/");
            requestMeta.put("scheme", "krpc");
            requestMeta.put("method", "RPC");
            requestMeta.put("secure", "true");
            requestMeta.put("requestURL", krpcService);
            requestMeta.put("requestURI", uri);
            requestMeta.put("remoteAddr", "");
            requestMeta.put("queryString", "");
            requestMeta.put("headers", requestHeaders);
            requestMeta.put("body", "");
            requestMeta.put("contextPath", "");
            requestMeta.put("replay-request", false);

            REQUEST_CONTEXT.set(requestMeta);
        }

        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_POOL.set(new HashSet<Object>());
        TAINT_HASH_CODES.set(new HashSet<Integer>());
    }

    /**
     * @return
     * @since 1.2.0
     */
    public static boolean isEnterHttp() {
        return ENTER_HTTP_ENTRYPOINT.isEnterEntry();
    }

    /**
     * @since 1.2.0
     */
    public static void leaveDubbo() {
        SCOPE_TRACKER.leaveDubbo();
    }

    /**
     * @since 1.2.0
     */
    public static boolean isExitedDubbo() {
        return SCOPE_TRACKER.isExitedDubbo();
    }

    /**
     * @since 1.2.0
     */
    public static boolean isFirstLevelDubbo() {
        return SCOPE_TRACKER.isFirstLevelDubbo();
    }


    public static void leaveKrpc() {
        SCOPE_TRACKER.leaveKrpc();
    }

    public static boolean isExitedKrpc() {
        return SCOPE_TRACKER.isExitedKrpc();
    }

    public static boolean isFirstLevelKrpc() {
        return SCOPE_TRACKER.isFirstLevelKrpc();
    }
}
