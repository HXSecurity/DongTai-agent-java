package io.dongtai.iast.core;

import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.LimiterManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.report.HookPointRateLimitReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.fallback.LimitFallbackSwitch;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.HookPointRateLimitReport;
import io.dongtai.iast.core.bytecode.enhance.plugin.limiter.RequestRateLimitReport;
import io.dongtai.iast.core.enums.RequestTypeEnum;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastServer;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.threadlocal.*;
import io.dongtai.iast.core.service.ServiceFactory;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.global.PerformanceFallback;
import io.dongtai.iast.core.utils.global.RequestRateLimiter;
import io.dongtai.log.DongTaiLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    public static final IastTrackMap TRACK_MAP = new IastTrackMap();
    public static final IastTaintPool TAINT_POOL = new IastTaintPool();
    public static final IastTaintHashCodes TAINT_HASH_CODES = new IastTaintHashCodes();
    public static final IastScopeTracker SCOPE_TRACKER = new IastScopeTracker();
    private static final IastServerPort LOGIN_LOGIC_WEIGHT = new IastServerPort();
    /**
     * 标记是否位于 IAST 的代码中，如果该值为 true 表示 iast 在运行中；如果 该值为 false 表示当前位置在iast的代码中，所有iast逻辑都bypass，直接退出
     */
    private static final BooleanThreadLocal DONGTAI_STATE = new BooleanThreadLocal(false);
    /**
     * 限制器统一管理器
     */
    private final LimiterManager limiterManager;
    public static final BooleanThreadLocal DONGTAI_HOOK_FALLBACK = new BooleanThreadLocal(false);
    /**
     * hook点高频命中限速器
     */
    public static final RateLimiterThreadLocal HOOK_RATE_LIMITER = new RateLimiterThreadLocal(PropertyUtils.getInstance());
    /**
     * 性能熔断开关
     */
    public static final PerformanceFallback DONGTAI_PERFORMANCE_FALLBACK = new PerformanceFallback(false);
    /**
     * 高频请求限速器
     */
    public static final RequestRateLimiter GLOBAL_REQUEST_RATE_LIMITER = new RequestRateLimiter(1, 5);
//    public static final GlobalRequestRateLimiter GLOBAL_REQUEST_RATE_LIMITER = new GlobalRequestRateLimiter(PropertyUtils.getInstance());

    public static IastServer SERVER;

    private static boolean logined = false;
    private static int reqCounts = 0;
    private static int enableLingzhi = 0;

    public static void turnOnLingzhi() {
        DONGTAI_STATE.set(true);
    }

    public static void turnOffLingzhi() {
        DONGTAI_STATE.set(false);
    }

    /**
     * Determine whether the current code flow enters the engine processing logic
     *
     * @return
     */
    public static Boolean isLingzhiRunning() {
        Boolean status = DONGTAI_STATE.get();
        return status != null && status;
    }

    /**
     * hook点是否降级
     */
    public static boolean isHookPointFallback() {
        return LimitFallbackSwitch.isRequestFallback();
    }

    /**
     * 打开hook点降级开关
     * 该开关打开后，在当前请求生命周期内，逻辑短路hook点
     */
    public static void openHookPointFallback(String className, String method, String methodSign, int hookType) {
        final double limitRate = EngineManager.getLimiterManager().getHookRateLimiter().getRate();
        DongTaiLog.info("HookPoint rate limit! hookType: " + hookType + ", method:" + className + "." + method
                + ", sign:" + methodSign + " ,rate:" + limitRate);
        HookPointRateLimitReport.sendReport(className, method, methodSign, hookType, limitRate);
        LimitFallbackSwitch.setHeavyHookFallback(true);
    }

    /**
     * 打开性能降级开关
     *
     * @param requestType 请求类型
     */
    public static void openPerformanceFallback(RequestTypeEnum requestType) {
        final double limitRate = EngineManager.GLOBAL_REQUEST_RATE_LIMITER.getRate();
        DongTaiLog.info("Request rate limit! RequestType:{},Rate:{}", requestType.getType(), limitRate);
        DONGTAI_PERFORMANCE_FALLBACK.setDefaultValue(true);
        RequestRateLimitReport.sendReport(requestType, limitRate);
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
        this.limiterManager = LimiterManager.newInstance(cfg.cfg);
    }

    public static LimiterManager getLimiterManager() {
        return instance.limiterManager;
    }

    /**
     * 清除当前线程的状态，避免线程重用导致的ThreadLocal产生内存泄漏的问题
     */
    public static void cleanThreadState() {
        EngineManager.LOGIN_LOGIC_WEIGHT.remove();
        EngineManager.ENTER_HTTP_ENTRYPOINT.remove();
        EngineManager.REQUEST_CONTEXT.remove();
        EngineManager.TRACK_MAP.remove();
        EngineManager.TAINT_POOL.remove();
        EngineManager.TAINT_HASH_CODES.remove();
        EngineManager.SCOPE_TRACKER.remove();
        LimitFallbackSwitch.clearHeavyHookFallback();
        EngineManager.getLimiterManager().getHookRateLimiter().remove();
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
        EngineManager.enableLingzhi = 1;
    }

    /**
     * 关闭检测引擎
     */
    public static void turnOffEngine() {
        EngineManager.enableLingzhi = 0;
    }

    /**
     * 检查灵芝引擎是否被开启
     *
     * @return true - 引擎已启动；false - 引擎未启动
     */
    public static boolean isEngineRunning() {
        return !LimitFallbackSwitch.isEngineFallback() && EngineManager.enableLingzhi == 1;
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
        // 尝试获取请求限速令牌，耗尽时打开性能熔断器
        if (!EngineManager.GLOBAL_REQUEST_RATE_LIMITER.acquire()) {
            EngineManager.openPerformanceFallback(RequestTypeEnum.HTTP);
        }

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

    /**
     * @param dubboService
     * @param attachments
     * @since 1.2.0
     */
    public static void enterDubboEntry(String dubboService, Map<String, String> attachments) {
        // 尝试获取请求限速令牌，耗尽时打开性能熔断器
        if (!EngineManager.GLOBAL_REQUEST_RATE_LIMITER.acquire()) {
            EngineManager.openPerformanceFallback(RequestTypeEnum.DUBBO);
        }

        if (attachments != null) {
            if (attachments.containsKey(ContextManager.getHeaderKey())) {
                ContextManager.getOrCreateGlobalTraceId(attachments.get(ContextManager.getHeaderKey()),
                        EngineManager.getAgentId());
            } else {
                attachments.put(ContextManager.getHeaderKey(), ContextManager.getSegmentId());
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
}
