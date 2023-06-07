package io.dongtai.iast.core;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.common.state.AgentState;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.IastServer;
import io.dongtai.iast.core.handler.hookpoint.controller.BodyBuffer;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
import io.dongtai.iast.core.service.ServerAddressReport;
import io.dongtai.iast.core.service.ServiceFactory;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.utils.threadlocal.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 存储全局信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EngineManager {

    private static EngineManager instance;
    private final int agentId;
    private final boolean saveBytecode;

    public static final RequestContext REQUEST_CONTEXT = new RequestContext();
    public static final IastTrackMap TRACK_MAP = new IastTrackMap();
    public static final IastTaintHashCodes TAINT_HASH_CODES = new IastTaintHashCodes();
    public static final TaintRangesPool TAINT_RANGES_POOL = new TaintRangesPool();
    public static final BodyBuffer BODY_BUFFER = new BodyBuffer();
    public static IastServer SERVER;
    public static final AgentState AGENT_STATE = AgentState.getInstance();

    private static final AtomicInteger reqCounts = new AtomicInteger(0);

    public static final BooleanThreadLocal ENTER_REPLAY_ENTRYPOINT = new BooleanThreadLocal(false);

    public static EngineManager getInstance() {
        return instance;
    }

    public static EngineManager getInstance(int agentId) {
        if (instance == null) {
            instance = new EngineManager(agentId);
        }
        return instance;
    }

    private EngineManager(int agentId) {
        PropertyUtils cfg = PropertyUtils.getInstance();
        this.saveBytecode = cfg.isEnableDumpClass();
        this.agentId = agentId;
    }

    /**
     * 清除当前线程的状态，避免线程重用导致的ThreadLocal产生内存泄漏的问题
     */
    public static void cleanThreadState() {
        EngineManager.REQUEST_CONTEXT.remove();
        EngineManager.TRACK_MAP.remove();
        EngineManager.TAINT_HASH_CODES.remove();
        EngineManager.TAINT_RANGES_POOL.remove();
        EngineManager.ENTER_REPLAY_ENTRYPOINT.remove();
        ContextManager.getContext().remove();
        ScopeManager.SCOPE_TRACKER.remove();
        EngineManager.BODY_BUFFER.remove();
    }

    public static void maintainRequestCount() {
        EngineManager.reqCounts.getAndIncrement();
    }

    /**
     * 获取引擎已检测的请求数量
     *
     * @return 产生的请求数量
     */
    public static int getRequestCount() {
        return EngineManager.reqCounts.get();
    }

    /**
     * 检查灵芝引擎是否被开启
     *
     * @return true - 引擎已启动；false - 引擎未启动
     */
    public static boolean isEngineRunning() {
        return AGENT_STATE.isRunning() && AGENT_STATE.getPendingState() == null && AGENT_STATE.isAllowReport();
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
            String url = null;
            String protocol = null;
            if (null != requestMeta.get("serverName")){
                url = (String) requestMeta.get("serverName");
            }else {
                url = "";
            }
            if(null != requestMeta.get("protocol")){
                protocol = (String) requestMeta.get("protocol");
            }else {
                protocol = "";
            }

            SERVER = new IastServer(
                    url,
                    (Integer) requestMeta.get("serverPort"),
                    protocol,
                    true
            );
            ServerAddressReport serverAddressReport = new ServerAddressReport(EngineManager.SERVER.getServerAddr(), EngineManager.SERVER.getServerPort(), EngineManager.SERVER.getProtocol());
            serverAddressReport.run();
        }
        Map<String, String> headers = (Map<String, String>) requestMeta.get("headers");
        String traceIdKey = ContextManager.getHeaderKey();
        if (headers.containsKey(traceIdKey)) {
            ContextManager.parseTraceId(headers.get(traceIdKey));
        } else {
            String newTraceId = ContextManager.currentTraceId();
            headers.put(traceIdKey, newTraceId);
        }
        REQUEST_CONTEXT.set(requestMeta);
        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_HASH_CODES.set(new HashSet<Long>());
        TAINT_RANGES_POOL.set(new HashMap<Long, TaintRanges>());
        ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).enter();
    }

    public static void enterDubboEntry(Map<String, Object> requestMeta) {
        REQUEST_CONTEXT.set(requestMeta);
        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_HASH_CODES.set(new HashSet<Long>());
        TAINT_RANGES_POOL.set(new HashMap<Long, TaintRanges>());
        ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_ENTRY).enter();
    }
}
