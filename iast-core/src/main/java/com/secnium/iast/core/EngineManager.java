package com.secnium.iast.core;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.middlewarerecognition.IastServer;
import com.secnium.iast.core.middlewarerecognition.ServerDetect;
import com.secnium.iast.core.report.AgentRegisterReport;
import com.secnium.iast.core.threadlocalpool.*;
import com.secnium.iast.core.util.LogUtils;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 存储全局信息
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EngineManager {

    private static final Logger logger = LogUtils.getLogger(EngineManager.class);
    private static EngineManager instance;
    private final PropertyUtils cfg;

    public static final BooleanTheadLocal ENTER_HTTP_ENTRYPOINT = new BooleanTheadLocal(false);
    public static final RequestContext REQUEST_CONTEXT = new RequestContext();
    public static final IastTrackMap TRACK_MAP = new IastTrackMap();
    public static final IastTaintPool TAINT_POOL = new IastTaintPool();
    public static final IastTaintHashCodes TAINT_HASH_CODES = new IastTaintHashCodes();
    public static final IastScopeTracker SCOPE_TRACKER = new IastScopeTracker();
    private static final IastServerPort LOGIN_LOGIC_WEIGHT = new IastServerPort();
    private static final BooleanTheadLocal LINGZHI_RUNNING = new BooleanTheadLocal(false);
    public static IastServer SERVER;

    private static final ArrayBlockingQueue<String> REPORTS = new ArrayBlockingQueue<String>(256);

    private static boolean logined = false;
    private static int reqCounts = 0;
    private static int enableLingzhi = 0;

    public static void turnOnLingzhi() {
        LINGZHI_RUNNING.set(true);
    }

    public static void turnOffLingzhi() {
        LINGZHI_RUNNING.set(false);
    }

    public static Boolean isLingzhiRunning() {
        return LINGZHI_RUNNING.get() != null && LINGZHI_RUNNING.get();
    }

    public static EngineManager getInstance() {
        return instance;
    }

    public static EngineManager getInstance(PropertyUtils cfg, Instrumentation inst) {
        if (instance == null) {
            if (cfg == null || inst == null) {
                return null;
            }
            instance = new EngineManager(cfg, inst);
        }
        return instance;
    }

    public static void setInstance() {
        instance = null;
    }

    private EngineManager(final PropertyUtils cfg,
                          final Instrumentation inst) {
        this.cfg = cfg;

        ServerDetect serverDetect = ServerDetect.getInstance();
        if (serverDetect.getWebserver() != null) {
            logger.info("WebServer [ name={}, path={} ]", serverDetect.getWebserver().getName(), serverDetect.getWebServerPath());
        }
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
    public static boolean isEngineEnable() {
        return EngineManager.enableLingzhi == 1;
    }

    public static boolean isEnableAllHook() {
        return instance.cfg.isEnableAllHook();
    }

    public PropertyUtils getCfg() {
        return cfg;
    }

    public static void sendNewReport(String report) {
        logger.debug(report);
        REPORTS.offer(report);
    }

    public static String getNewReport() {
        return REPORTS.poll();
    }

    public static boolean hasNewReport() {
        return !REPORTS.isEmpty();
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

    public static boolean isTopLovelSink() {
        return SCOPE_TRACKER.isFirstLevelSink();
    }

    public static boolean hasTaintValue() {
        return SCOPE_TRACKER.hasTaintValue();
    }

    public static String getNamespace() {
        return instance.cfg.getNamespace();
    }

    public static boolean isEnableDumpClass() {
        return instance.cfg.isEnableDumpClass();
    }

    public static void enterHttpEntry(Map<String, Object> requestMeta) {
        if (null == SERVER) {
            SERVER = new IastServer(
                    (String) requestMeta.get("serverName"),
                    (Integer) requestMeta.get("serverPort"),
                    true
            );
            AgentRegisterReport.send();
        }
        ENTER_HTTP_ENTRYPOINT.enterHttpEntryPoint();
        REQUEST_CONTEXT.set(requestMeta);
        TRACK_MAP.set(new HashMap<Integer, MethodEvent>(1024));
        TAINT_POOL.set(new HashSet<Object>());
        TAINT_HASH_CODES.set(new HashSet<Integer>());
    }
}
