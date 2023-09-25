package io.dongtai.iast.core.handler.hookpoint;

import io.dongtai.iast.common.config.ConfigBuilder;
import io.dongtai.iast.common.config.ConfigKey;
import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.common.string.StringUtils;
import io.dongtai.iast.core.AgentEngine;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.bypass.BlackUrlBypass;
import io.dongtai.iast.core.handler.hookpoint.api.DubboApiGatherThread;
import io.dongtai.iast.core.handler.hookpoint.api.SpringGatherApiThread;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.*;
import io.dongtai.iast.core.handler.hookpoint.graphy.GraphBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.*;
import io.dongtai.iast.core.handler.hookpoint.service.trace.DubboService;
import io.dongtai.iast.core.handler.hookpoint.service.trace.FeignService;
import io.dongtai.iast.core.handler.hookpoint.service.trace.HttpService;
import io.dongtai.iast.core.utils.matcher.ConfigMatcher;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.lang.dongtai.SpyDispatcher;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 1.3.1
 */
public class SpyDispatcherImpl implements SpyDispatcher {

    public static final AtomicInteger INVOKE_ID_SEQUENCER = new AtomicInteger(1);

    @Override
    public void enterScope(int id) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            Scope scope = Scope.getScope(id);
            if (scope == null) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getScope(scope).enter();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public boolean inScope(int id) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return false;
            }
            Scope scope = Scope.getScope(id);
            if (scope == null) {
                return false;
            }
            return ScopeManager.SCOPE_TRACKER.getScope(scope).in();
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public boolean isFirstLevelScope(int id) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return false;
            }
            Scope scope = Scope.getScope(id);
            if (scope == null) {
                return false;
            }
            return ScopeManager.SCOPE_TRACKER.getScope(scope).isFirst();
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public void leaveScope(int id) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            Scope scope = Scope.getScope(id);
            if (scope == null) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getScope(scope).leave();
        } catch (Throwable ignore) {
        }
    }

    /**
     * mark for enter Http Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterHttp() {
        if (!EngineManager.isEngineRunning()) {
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_REQUEST).enter();
        } catch (Throwable ignore) {
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
        if (!EngineManager.isEngineRunning()) {
            EngineManager.cleanThreadState();
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_REQUEST).leave();
            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_REQUEST).in()
                    && ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                EngineManager.maintainRequestCount();
                GraphBuilder.buildAndReport();
                EngineManager.cleanThreadState();
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_LEAVE_HTTP_FAILED"), e);
            EngineManager.cleanThreadState();
        }
    }

    /**
     * Determines whether it is a layer 1 HTTP entry
     *
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelHttp() {
        if (!EngineManager.isEngineRunning()) {
            return false;
        }
        try {
            return ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_REQUEST).isFirst();
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public void collectHttpRequest(Object obj, Object req, Object resp, StringBuffer requestURL, String requestURI,
                                   String queryString, String method, String protocol, String scheme,
                                   String serverName, String contextPath, String remoteAddr,
                                   boolean isSecure, int serverPort, Enumeration<?> headerNames) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!EngineManager.isEngineRunning()) {
                return;
            }

            HttpImpl.createClassLoader(req);

            Map<String, String> headers = HttpImpl.parseRequestHeaders(req, headerNames);
            Map<String, Object> requestMeta = new HashMap<String, Object>() {{
                put("requestURL", requestURL);
                put("requestURI", requestURI);
                put("queryString", queryString);
                put("method", method);
                put("protocol", protocol);
                put("scheme", scheme);
                put("serverName", serverName);
                put("contextPath", contextPath);
                put("remoteAddr", "0:0:0:0:0:0:0:1".equals(remoteAddr) ? "127.0.0.1" : remoteAddr);
                put("secure", isSecure);
                put("serverPort", serverPort);
                put("headers", headers);
                put("replay-request", !StringUtils.isEmpty(headers.get("dongtai-replay-id")));
            }};
            if (ConfigMatcher.getInstance().getBlackUrl(requestMeta)) {
                BlackUrlBypass.setIsBlackUrl(true);
                return;
            }
            if (null != headers.get(BlackUrlBypass.getHeaderKey()) && headers.get(BlackUrlBypass.getHeaderKey()).equals("true")) {
                BlackUrlBypass.setIsBlackUrl(true);
                return;
            }
            HttpImpl.solveHttpRequest(obj, req, resp, requestMeta);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "request", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void onServletInputStreamRead(int ret, String desc, Object stream, byte[] bs, int offset, int len) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (!EngineManager.isEngineRunning()) {
                return;
            }

            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                return;
            }

            HttpImpl.onServletInputStreamRead(ret, desc, stream, bs, offset, len);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "request body", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void collectHttpResponse(Object obj, Object req, Object resp, Collection<?> headerNames, int status) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                return;
            }

            HttpImpl.solveHttpResponse(resp, req, resp, headerNames, status);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "response header", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void onServletOutputStreamWrite(String desc, Object stream, int b, byte[] bs, int offset, int len) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (!EngineManager.isEngineRunning()) {
                return;
            }

            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                return;
            }

            HttpImpl.onServletOutputStreamWrite(desc, stream, b, bs, offset, len);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "response body", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void onPrintWriterWrite(String desc, Object writer, int b, String s, char[] cs, int offset, int len) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (!EngineManager.isEngineRunning()) {
                return;
            }

            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                return;
            }

            HttpImpl.onPrintWriterWrite(desc, writer, b, s, cs, offset, len);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "response body", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void enterDubbo() {
        if (!EngineManager.isEngineRunning()) {
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_REQUEST).enter();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void leaveDubbo(Object channel, Object request) {
        if (!EngineManager.isEngineRunning()) {
            EngineManager.cleanThreadState();
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_REQUEST).leave();
            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_REQUEST).in()
                    && ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_ENTRY).in()) {
                EngineManager.maintainRequestCount();
                GraphBuilder.buildAndReport();
                EngineManager.cleanThreadState();
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_LEAVE_DUBBO_FAILED"), e);
            EngineManager.cleanThreadState();
        }
    }

    @Override
    public boolean isFirstLevelDubbo() {
        if (!EngineManager.isEngineRunning()) {
            return false;
        }
        try {
            return ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_REQUEST).isFirst();
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public void collectDubboRequest(Object handler, Object channel, Object request,
                                    String url, InetSocketAddress remoteAddress,
                                    boolean isTwoWay, boolean isEvent, boolean isBroken, boolean isHeartbeat) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!EngineManager.isEngineRunning()) {
                return;
            }

            if (isEvent || isBroken || isHeartbeat || !isTwoWay) {
                return;
            }

            DubboImpl.solveDubboRequest(handler, channel, request, url, remoteAddress.getAddress().getHostAddress());
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_DUBBO_FAILED"), "request", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }

        DubboImpl.createClassLoader(handler);

        // TODO 2023-6-27 19:17:18 测试ClassLoader的GC回收
        // 收集Dubbo的api
        try {
            DubboApiGatherThread.gather(handler.getClass());
        } catch (Throwable e) {
            DongTaiLog.error("SpyDispatcherImpl.collectDubboRequest collection dubbo api error", e);
        }

    }

    @Override
    public void collectDubboRequestSource(Object handler, Object invocation, String methodName,
                                          Object[] arguments, Class<?>[] argumentTypes, Map<String, ?> headers,
                                          String hookClass, String hookMethod, String hookSign) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!EngineManager.isEngineRunning()) {
                return;
            }

            DubboImpl.collectDubboRequestSource(handler, invocation, methodName, arguments, argumentTypes, headers,
                    hookClass, hookMethod, hookSign, INVOKE_ID_SEQUENCER);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_DUBBO_FAILED"), "request source", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    @Override
    public void collectDubboResponse(Object result, byte status) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!EngineManager.isEngineRunning()) {
                return;
            }

            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_REQUEST).isFirst()
                    || !ScopeManager.SCOPE_TRACKER.getScope(Scope.DUBBO_ENTRY).in()
                    || ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_REQUEST).in()) {
                return;
            }

            DubboImpl.collectDubboResponse(result, status);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SPY_COLLECT_DUBBO_FAILED"), "response", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSource() {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterSource();
        } catch (Throwable ignore) {
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
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveSource();
        } catch (Throwable ignore) {
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
            if (!EngineManager.isEngineRunning()) {
                return false;
            }
            return ScopeManager.SCOPE_TRACKER.inEnterEntry()
                    && ScopeManager.SCOPE_TRACKER.getPolicyScope().isValidSource();
        } catch (Throwable ignore) {
            return false;
        }
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterPropagator(boolean skipScope) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterPropagator(skipScope);
        } catch (Throwable ignore) {
        }
    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leavePropagator(boolean skipScope) {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leavePropagator(skipScope);
        } catch (Throwable ignore) {
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
            if (!EngineManager.isEngineRunning()) {
                return false;
            }
            return ScopeManager.SCOPE_TRACKER.inEnterEntry()
                    && ScopeManager.SCOPE_TRACKER.getPolicyScope().isValidPropagator();
        } catch (Throwable ignore) {
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
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterSink();
        } catch (Throwable ignore) {
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
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveSink();
        } catch (Throwable ignore) {
        }
    }

    /**
     * mark for enter validator entry point
     */
    @Override
    public boolean enterValidator() {
        if (!EngineManager.isEngineRunning()) {
            return false;
        }
        return !ScopeManager.SCOPE_TRACKER.inAgent() && ScopeManager.SCOPE_TRACKER.inEnterEntry();
    }

    /**
     * Determines whether it is a layer 1 Sink entry
     *
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSink() {
        try {
            if (!EngineManager.isEngineRunning()) {
                return false;
            }
            return ScopeManager.SCOPE_TRACKER.inEnterEntry()
                    && ScopeManager.SCOPE_TRACKER.getPolicyScope().isValidSink();
        } catch (Throwable ignore) {
            return false;
        }
    }

    @Override
    public void enterIgnoreInternal() {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterIgnoreInternal();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void leaveIgnoreInternal() {
        try {
            if (!EngineManager.isEngineRunning()) {
                return;
            }
            if (ScopeManager.SCOPE_TRACKER.inAgent() || !ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return;
            }
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveIgnoreInternal();
        } catch (Throwable ignore) {
        }
    }

    @Override
    public void reportService(String category, String type, String host, String port, String handler) {
        // @TODO: refactor
    }

    @Override
    public boolean isReplayRequest() {
        return EngineManager.ENTER_REPLAY_ENTRYPOINT.get();
    }

    @Override
    public boolean isNotReplayRequest() {
        return !EngineManager.ENTER_REPLAY_ENTRYPOINT.get();
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework,
                                     String className, String matchClassName, String methodName, String methodSign, boolean isStatic,
                                     int hookType) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!isCollectAllowed(true)) {
                return false;
            }

            // 收集Spring MVC的API
            if (HookType.SPRINGAPPLICATION.equals(hookType)) {
                SpringGatherApiThread.gather(retValue);
            }
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_COLLECT_HTTP_FAILED"), "", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    @Override
    public boolean collectMethod(Object instance, Object[] parameters, Object retObject, String policyKey,
                                 String className, String matchedClassName, String methodName, String signature,
                                 boolean isStatic) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            PolicyNode policyNode = getPolicyNode(policyKey);
            if (policyNode == null) {
                return false;
            }

            if (!isCollectAllowed(false)) {
                return false;
            }

            MethodEvent event = new MethodEvent(className, matchedClassName, methodName,
                    signature, instance, parameters, retObject);

            if ((policyNode instanceof SourceNode)) {
                SourceImpl.solveSource(event, (SourceNode) policyNode, INVOKE_ID_SEQUENCER);
                return true;
            } else if ((policyNode instanceof PropagatorNode)) {
                PropagatorImpl.solvePropagator(event, (PropagatorNode) policyNode, INVOKE_ID_SEQUENCER);
                return true;
            } else if ((policyNode instanceof SinkNode)) {
                SinkImpl.solveSink(event, (SinkNode) policyNode);
                return true;
            } else if ((policyNode instanceof ValidatorNode)) {
                ValidatorImpl.solveValidator(event,(ValidatorNode)policyNode, INVOKE_ID_SEQUENCER);
                return true;
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_COLLECT_METHOD_FAILED"), e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    @Override
    public boolean traceFeignInvoke(Object instance, Object[] parameters,
                                    String className, String methodName, String signature) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (!isCollectAllowed(false)) {
                return false;
            }

            MethodEvent event = new MethodEvent(className, className, methodName,
                    signature, instance, parameters, null);

            FeignService.solveSyncInvoke(event, INVOKE_ID_SEQUENCER);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_TRACE_FEIGN_INVOKE_FAILED"), e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    @Override
    public boolean traceDubboInvoke(Object instance, String url, Object invocation, Object[] arguments,
                                    Map<String, String> headers, String className, String methodName,
                                    String signature) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            if (!isCollectAllowed(false)) {
                return false;
            }

            MethodEvent event = new MethodEvent(className, className, methodName,
                    signature, instance, arguments, null);

            DubboService.solveSyncInvoke(event, invocation, url, headers, INVOKE_ID_SEQUENCER);
        } catch (Throwable e) {
            DongTaiLog.error(ErrorCode.get("SPY_TRACE_DUBBO_CONSUMER_INVOKE_FAILED"), e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    @Override
    public boolean isSkipCollectDubbo(Object invocation) {
        if (BlackUrlBypass.isBlackUrl()) {
            Method setAttachmentMethod;
            try {
                setAttachmentMethod = invocation.getClass().getMethod("setAttachment", String.class, String.class);
                setAttachmentMethod.setAccessible(true);
                setAttachmentMethod.invoke(invocation, BlackUrlBypass.getHeaderKey(), BlackUrlBypass.isBlackUrl().toString());
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                DongTaiLog.error(ErrorCode.get("BYPASS_FAILED_DUBBO"), e);
            }
        }
        return false;
    }

    @Override
    public boolean isSkipCollectFeign(Object instance) {
        if (BlackUrlBypass.isBlackUrl()) {
            Field metadataField;
            try {
                metadataField = instance.getClass().getDeclaredField("metadata");
                metadataField.setAccessible(true);
                Object metadata = metadataField.get(instance);
                Method templateMethod = metadata.getClass().getMethod("template");
                Object template = templateMethod.invoke(metadata);
                Method addHeaderMethod = template.getClass().getDeclaredMethod("header", String.class, String[].class);
                addHeaderMethod.setAccessible(true);
                ConcurrentHashMap<String, Collection<String>> headers = new ConcurrentHashMap<>();
                synchronized (template){
                    /*
                     * @todo 在高并发情况下，由于SynchronousMethodHandler复用的原因，会导致多线程下同时修改的问题
                     *   所以添加了对象锁，后续是否可以考虑更改为拦截器
                     */
//                    addHeaderMethod.invoke(template, BlackUrlBypass.getHeaderKey(), new String[]{});
//                    addHeaderMethod.invoke(template, BlackUrlBypass.getHeaderKey(), new String[]{BlackUrlBypass.isBlackUrl().toString()});
                    addHeaderMethod.invoke(template, BlackUrlBypass.getHeaderKey(), new String[]{"true"});
                }
            } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                DongTaiLog.error(ErrorCode.get("BYPASS_FAILED_FEIGN"), e);
            }
        }
        return false;
    }

    @Override
    public boolean skipCollect(Object instance, Object[] parameters, Object retObject, String policyKey,
                               String className, String matchedClassName, String methodName, String signature,
                               boolean isStatic) {
        if (BlackUrlBypass.isBlackUrl()) {
            MethodEvent event = new MethodEvent(className, matchedClassName, methodName,
                    signature, instance, parameters, retObject);
            PolicyNode policyNode = getPolicyNode(policyKey);
            if (policyNode == null) {
                return false;
            }
            HttpService httpService = new HttpService();
            if (httpService.match(event, policyNode)) {
                httpService.addBypass(event);
                return true;
            }
        }
        return false;
    }

    private boolean isCollectAllowed(boolean isEnterEntry) {
        if (!EngineManager.isEngineRunning()) {
            return false;
        }

        if (!isEnterEntry) {
            if (!ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return false;
            }

            if (ScopeManager.SCOPE_TRACKER.getPolicyScope().isOverCapacity()) {
                return false;
            }

            if (EngineManager.TRACK_MAP.get() == null) {
                return false;
            }

            Integer methodPoolMaxSize = ConfigBuilder.getInstance().get(ConfigKey.REPORT_MAX_METHOD_POOL_SIZE);
            if (methodPoolMaxSize != null && methodPoolMaxSize > 0
                    && EngineManager.TRACK_MAP.get().size() >= methodPoolMaxSize) {
                ScopeManager.SCOPE_TRACKER.getPolicyScope().setOverCapacity(true);
                DongTaiLog.warn(ErrorCode.get("SPY_METHOD_POOL_OVER_CAPACITY"), methodPoolMaxSize);
                return false;
            }
        }

        return true;
    }

    private PolicyNode getPolicyNode(String policyKey) {
        AgentEngine agentEngine = AgentEngine.getInstance();
        PolicyManager policyManager = agentEngine.getPolicyManager();
        if (policyManager == null) {
            return null;
        }
        Policy policy = policyManager.getPolicy();
        if (policy == null) {
            return null;
        }

        return policy.getPolicyNode(policyKey);
    }
}
