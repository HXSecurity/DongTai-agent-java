package com.secnium.iast.core.handler;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.enhance.plugins.api.spring.SpringApplicationImpl;
import com.secnium.iast.core.handler.controller.HookType;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.controller.impl.PropagatorImpl;
import com.secnium.iast.core.handler.controller.impl.SinkImpl;
import com.secnium.iast.core.handler.controller.impl.SourceImpl;
import com.secnium.iast.core.handler.graphy.GraphBuilder;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.ThrowableUtils;
import java.lang.iast.inject.Injecter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AOP生成的事件的处理中心
 *
 * @author dongzhiyong@huoxian.cn
 */
public class EventListenerHandlers {

    /**
     * 调用序列生成器
     */
    public static final AtomicInteger INVOKE_ID_SEQUENCER = new AtomicInteger(1000);

    public static void onBefore(final String framework,
            final String javaClassName,
            final String matchClassName,
            final String javaMethodName,
            final String javaMethodDesc,
            final Object object,
            final Object[] argumentArray,
            final Object retValue,
            final String signature,
            final boolean isStatic,
            final int hookType
    ) {
        // 如果已经进入scope，则检查是否遇到suorce点、sink点等
        if (HookType.HTTP.equals(hookType)) {
            if (!EngineManager.isLingzhiRunning()) {
                EngineManager.turnOnLingzhi();
            }
        }

        if (EngineManager.isLingzhiRunning()) {
            try {
                EngineManager.turnOffLingzhi();

                if (HookType.SPRINGAPPLICATION.equals(hookType)) {
                    MethodEvent event = new MethodEvent(0, -1, javaClassName, matchClassName, javaMethodName,
                            javaMethodDesc, signature, object, argumentArray, retValue, framework, isStatic, null);
                    SpringApplicationImpl.getWebApplicationContext(event);
                } else {
                    boolean isEnterHttpEntryPoint = EngineManager.ENTER_HTTP_ENTRYPOINT.isEnterHttp();
                    boolean isHttpEntryMethod = HookType.HTTP.equals(hookType) || HookType.DUBBO.equals(hookType);
                    if (isEnterHttpEntryPoint || isHttpEntryMethod) {
                        MethodEvent event = new MethodEvent(0, -1, javaClassName, matchClassName, javaMethodName,
                                javaMethodDesc, signature, object, argumentArray, retValue, framework, isStatic, null);
                        if (HookType.HTTP.equals(hookType)) {
                            HttpImpl.solveHttp(event);
                        } else if (HookType.DUBBO.equals(hookType)) {
                            System.out.println("Enter Dubbo");
                        } else if (HookType.PROPAGATOR.equals(hookType) && !EngineManager.TAINT_POOL.get().isEmpty()) {
                            PropagatorImpl.solvePropagator(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SOURCE.equals(hookType)) {
                            SourceImpl.solveSource(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SINK.equals(hookType)&& !EngineManager.TAINT_POOL.get().isEmpty()) {
                            SinkImpl.solveSink(event);
                        }
                    }
                }
            } catch (Exception e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } finally {
                EngineManager.turnOnLingzhi();
            }
        }
    }

    public static Object onReturn(final int listenerId,
            final Class<?> spyRetClassInTargetClassLoader,
            final Object object) throws Throwable {
        // 判断sign是否需要hook
        Injecter.Ret ret = null;
        if (EngineManager.isLingzhiRunning()) {
            try {
                EngineManager.turnOffLingzhi();
                // todo: 后续重放时，启用
            } catch (Exception e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
                ret = Injecter.Ret.newInstanceForReturn(object);
            } finally {
                EngineManager.turnOnLingzhi();
            }
        } else {
            ret = Injecter.Ret.newInstanceForReturn(object);
        }
        return ret;
    }

    public static Object onThrows(final int listenerId,
            final Class<?> spyRetClassInTargetClassLoader,
            final Throwable throwable) throws Throwable {
        return null;
    }

    public static void enterPropagator() {
        try {
            EngineManager.SCOPE_TRACKER.enterPropagation();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static void leavePropagator() {
        try {
            EngineManager.SCOPE_TRACKER.leavePropagation();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static boolean isFirstLevelPropagator() {
        try {
            return EngineManager.SCOPE_TRACKER.isFirstLevelPropagator();
        } catch (Exception e) {
            return false;
        }
    }

    public static void enterSource() {
        try {
            EngineManager.SCOPE_TRACKER.enterSource();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static void leaveSource() {
        try {
            EngineManager.SCOPE_TRACKER.leaveSource();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static boolean isFirstLevelSource() {
        try {
            return EngineManager.SCOPE_TRACKER.isFirstLevelSource();
        } catch (Exception e) {
            return false;
        }
    }

    public static void enterSink() {
        try {
            EngineManager.SCOPE_TRACKER.enterSink();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static void leaveSink() {
        try {
            EngineManager.SCOPE_TRACKER.leaveSink();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    public static boolean isFirstLevelSink() {
        try {
            return EngineManager.isTopLovelSink();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasTaintValue() {
        try {
            return EngineManager.hasTaintValue();
        } catch (Exception e) {
            return false;
        }
    }

    public static void enterHttp() {
        try {
            EngineManager.SCOPE_TRACKER.enterHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
    }

    /**
     * 离开HTTP入口时，维护当前线程的状态
     */
    public static void leaveHttp(Object response) {
        try {
            EngineManager.SCOPE_TRACKER.leaveHttp();
            if (EngineManager.SCOPE_TRACKER.isExitedHttp() && EngineManager.ENTER_HTTP_ENTRYPOINT.isEnterHttp()) {
                EngineManager.maintainRequestCount();
                GraphBuilder.buildAndReport(response);
                EngineManager.cleanThreadState();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            EngineManager.cleanThreadState();
        }
    }

    /**
     * 根据线程状态判断是否是第一次进入HTTP入口，只有第一次进入HTTP才需要捕获HTTP相关数据
     *
     * @return true：是，false：不是
     */
    public static boolean isFirstLevelHttp() {
        try {
            return EngineManager.SCOPE_TRACKER.isFirstLevelHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return false;
    }

    /**
     * Wrap the Request object and get the request packet
     *
     * @param req       The instantiated object of HttpServletRequest(Servlet-API、Jakarta-API)
     * @param isJakarta Whether it is the request object of jakarta-api
     * @return The request object wrapped by RequestWrapper, which can call inputStream/Reader repeatedly
     */
    public static Object cloneRequest(Object req, boolean isJakarta) {
        return HttpImpl.cloneRequest(req, isJakarta);
    }

    public static boolean isReplayRequest() {
        try {
            return (Boolean) EngineManager.REQUEST_CONTEXT.get().get("replay-request");
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return false;
    }

    /**
     * Wrap the Response object and get the response packet
     *
     * @param response  The instantiated object of HttpServletResponse(Servlet-API、Jakarta-API)
     * @param isJakarta Whether it is the request object of jakarta-api
     * @return
     */
    public static Object cloneResponse(Object response, boolean isJakarta) {
        return HttpImpl.cloneResponse(response);
    }
}
