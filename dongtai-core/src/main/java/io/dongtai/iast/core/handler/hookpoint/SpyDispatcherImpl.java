package io.dongtai.iast.core.handler.hookpoint;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.*;
import io.dongtai.iast.core.handler.hookpoint.graphy.GraphBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.service.ErrorLogReport;
import io.dongtai.log.DongTaiLog;

import java.lang.dongtai.SpyDispatcher;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 1.3.1
 */
public class SpyDispatcherImpl implements SpyDispatcher {

    public static final AtomicInteger INVOKE_ID_SEQUENCER = new AtomicInteger(1);

    /**
     * mark for enter Http Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterHttp() {
        try {
            EngineManager.SCOPE_TRACKER.enterHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Http Entry Point
     *
     * @param response HttpResponse Object for collect http response body.
     * @since 1.3.1
     */
    @Override
    public void leaveHttp(Object request, Object response) {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.turnOffDongTai();

                EngineManager.SCOPE_TRACKER.leaveHttp();
                if (EngineManager.SCOPE_TRACKER.isExitedHttp() && EngineManager.isEnterHttp()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(request, response);
                    EngineManager.cleanThreadState();
                }

                EngineManager.turnOnDongTai();
            }

        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 HTTP entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelHttp() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.SCOPE_TRACKER
                    .isFirstLevelHttp();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * clone request object for copy http post body.
     *
     * @param req       HttpRequest Object
     * @param isJakarta true if jakarta-servlet-api else false
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneRequest(Object req, boolean isJakarta) {
        return HttpImpl.cloneRequest(req, isJakarta);
    }

    /**
     * clone response object for copy http response data.
     *
     * @param response
     * @param isJakarta
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneResponse(Object response, boolean isJakarta) {
        return HttpImpl.cloneResponse(response, isJakarta);
    }

    /**
     * mark for enter Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterDubbo() {
        try {
            EngineManager.SCOPE_TRACKER.enterDubbo();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveDubbo() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.turnOffDongTai();

                EngineManager.leaveDubbo();
                if (EngineManager.isExitedDubbo() && !EngineManager.isEnterHttp()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(null, null);
                    EngineManager.cleanThreadState();
                }

                EngineManager.turnOnDongTai();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelDubbo() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isFirstLevelDubbo();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * mark for enter Krpc Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterKrpc() {
        try {
            EngineManager.SCOPE_TRACKER.enterKrpc();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Krpc Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveKrpc() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.turnOffDongTai();

                EngineManager.leaveKrpc();
                if (EngineManager.isExitedKrpc() && !EngineManager.isEnterHttp()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(null, null);
                    EngineManager.cleanThreadState();
                }

                EngineManager.turnOnDongTai();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 Krpc entry
     *
     * @return true if is a layer 1 Krpc entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelKrpc() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isFirstLevelKrpc();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * mark for enter Krpc Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterKrpcHttp() {
        try {
            EngineManager.SCOPE_TRACKER.enterKrpc();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Krpc Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveKrpcHttp() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.turnOffDongTai();

                EngineManager.leaveKrpc();
                if (EngineManager.isExitedKrpc() && !EngineManager.isEnterHttp()) {
                    EngineManager.maintainRequestCount();
                    GraphBuilder.buildAndReport(EngineManager.REQUEST_CONTEXT.get(), EngineManager.RESPONSE_CONTEXT.get());
                    EngineManager.cleanThreadState();
                }

                EngineManager.turnOnDongTai();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 Krpc entry
     *
     * @return true if is a layer 1 Krpc entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelKrpcHttp() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isFirstLevelKrpc();
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
        return false;
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSource() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.enterSource();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSource() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.leaveSource();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSource() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isDongTaiRunning() && EngineManager.SCOPE_TRACKER
                    .isFirstLevelSource();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterPropagator() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.enterPropagation();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leavePropagator() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.leavePropagation();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Propagator entry
     *
     * @return true if is a layer 1 Propagator entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelPropagator() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isDongTaiRunning() && EngineManager.SCOPE_TRACKER.isFirstLevelPropagator();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSink() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.enterSink();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSink() {
        try {
            if (EngineManager.isDongTaiRunning()) {
                EngineManager.SCOPE_TRACKER.leaveSink();
            }
        } catch (Exception e) {
            ErrorLogReport.sendErrorLog(e);
        }
    }

    /**
     * Determines whether it is a layer 1 Sink entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSink() {
        try {
            return EngineManager.isEngineRunning() && EngineManager.isDongTaiRunning() && EngineManager.isTopLevelSink();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @param instance       current class install object value, null if static class
     * @param argumentArray
     * @param retValue
     * @param framework
     * @param className
     * @param matchClassName
     * @param methodName
     * @param methodSign
     * @param isStatic
     * @param hookType
     * @return false if normal else throw a exception
     * @since 1.3.1
     */
    @Override
    public boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework,
                                     String className, String matchClassName, String methodName, String methodSign, boolean isStatic,
                                     int hookType) {
        if (!EngineManager.isDongTaiRunning() && (HookType.HTTP.equals(hookType) || HookType.RPC.equals(hookType))) {
            EngineManager.turnOnDongTai();
        }

        if (EngineManager.isDongTaiRunning()) {
            try {
                EngineManager.turnOffDongTai();

                if (HookType.SPRINGAPPLICATION.equals(hookType)) {
                    MethodEvent event = new MethodEvent(0, -1, className, matchClassName, methodName,
                            methodSign, methodSign, instance, argumentArray, retValue, framework, isStatic, null);
                    SpringApplicationImpl.getWebApplicationContext(event);
                } else {
                    boolean isEnterEntryPoint = EngineManager.isEnterHttp() || EngineManager.isFirstLevelDubbo() || EngineManager.isFirstLevelKrpc();
                    boolean isEntryPointMethod = HookType.HTTP.equals(hookType) || HookType.RPC.equals(hookType);
                    if (isEnterEntryPoint || isEntryPointMethod) {
                        MethodEvent event = new MethodEvent(0, -1, className, matchClassName, methodName,
                                methodSign, methodSign, instance, argumentArray, retValue, framework, isStatic, null);
                        if (HookType.HTTP.equals(hookType)) {
                            HttpImpl.solveHttp(event);
                        } else if (HookType.RPC.equals(hookType)) {
                            solveRPC(framework, event);
                        } else if (HookType.PROPAGATOR.equals(hookType) && !EngineManager.TAINT_POOL.get().isEmpty()) {
                            PropagatorImpl.solvePropagator(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SOURCE.equals(hookType)) {
                            SourceImpl.solveSource(event, INVOKE_ID_SEQUENCER);
                        } else if (HookType.SINK.equals(hookType) && !EngineManager.TAINT_POOL.get().isEmpty()) {
                            SinkImpl.solveSink(event);
                        }
                    }
                }
            } catch (Exception e) {
                DongTaiLog.debug(e);
            } finally {
                EngineManager.turnOnDongTai();
            }
        }
        return false;
    }

    private void solveRPC(String framework, MethodEvent event) {
        switch (framework) {
            case "krpc":
                KrpcImpl.solveKrpc(event, SpyDispatcherImpl.INVOKE_ID_SEQUENCER);
                break;
            case "krpc_api" :
                KrpcImpl.solveKrpcApi(event, SpyDispatcherImpl.INVOKE_ID_SEQUENCER);
                break;
            case "krpc_http" :
                KrpcImpl.solveKrpcHttpEnter(event, SpyDispatcherImpl.INVOKE_ID_SEQUENCER);
                break;
            case "krpc_http_exit" :
                KrpcImpl.solveKrpcHttpExit(event, SpyDispatcherImpl.INVOKE_ID_SEQUENCER);
                break;
        }
    }

    @Override
    public void toStringUtf8(Object ref) {
        System.out.println(ref);
        System.out.println(System.identityHashCode(ref));
    }
}
