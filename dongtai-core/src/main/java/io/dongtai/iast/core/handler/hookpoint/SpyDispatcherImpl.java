package io.dongtai.iast.core.handler.hookpoint;

import com.secnium.iast.core.AgentEngine;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationImpl;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.handler.hookpoint.controller.impl.*;
import io.dongtai.iast.core.handler.hookpoint.framework.dubbo.DubboHandler;
import io.dongtai.iast.core.handler.hookpoint.framework.grpc.GrpcHandler;
import io.dongtai.iast.core.handler.hookpoint.graphy.GraphBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.*;
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
        if (!EngineManager.isEngineRunning()) {
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getHttpRequestScope().enter();
        } catch (Exception ignore) {
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
            return;
        }
        try {
            ScopeManager.SCOPE_TRACKER.getHttpRequestScope().leave();
            if (!ScopeManager.SCOPE_TRACKER.getHttpRequestScope().in()
                    && ScopeManager.SCOPE_TRACKER.getHttpEntryScope().in()) {
                EngineManager.maintainRequestCount();
                GraphBuilder.buildAndReport(request, response);
                EngineManager.cleanThreadState();
            }
        } catch (Exception e) {
            DongTaiLog.error("leave http failed", e);
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
            return ScopeManager.SCOPE_TRACKER.getHttpRequestScope().isFirst();
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * clone request object for copy http post body.
     *
     * @param req       HttpRequest Object
     * @param isJakarta true if jakarta-servlet-api else false
     * @since 1.3.1
     */
    @Override
    public Object cloneRequest(Object req, boolean isJakarta) {
        if (!EngineManager.isEngineRunning()) {
            return req;
        }
        try {
            return HttpImpl.cloneRequest(req, isJakarta);
        } catch (Exception ignore) {
            return req;
        }
    }

    /**
     * clone response object for copy http response data.
     * @since 1.3.1
     */
    @Override
    public Object cloneResponse(Object response, boolean isJakarta) {
        if (!EngineManager.isEngineRunning()) {
            return response;
        }
        try {
            return HttpImpl.cloneResponse(response, isJakarta);
        } catch (Exception ignore) {
            return response;
        }
    }

    /**
     * mark for enter Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterDubbo() {
        // @TODO: refactor
    }

    /**
     * mark for leave Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveDubbo(Object invocation, Object rpcResult) {
        // @TODO: refactor
    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelDubbo() {
        // @TODO: refactor
        return false;
    }

    @Override
    public void enterKafka(Object record) {
        // @TODO: refactor
    }

    @Override
    public void kafkaBeforeSend(Object record) {
        // @TODO: refactor
    }

    @Override
    public void kafkaAfterPoll(Object record) {
        // @TODO: refactor
    }

    @Override
    public void leaveKafka() {
        // @TODO: refactor
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
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
        } catch (Exception ignore) {
        }
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
        } catch (Exception ignore) {
            return false;
        }
    }

    @Override
    public Object clientInterceptor(Object channel) {
        return GrpcHandler.interceptChannel(channel);
    }

    @Override
    public Object serverInterceptor(Object serverServiceDefinition) {
        return GrpcHandler.interceptService(serverServiceDefinition);
    }

    @Override
    public void startGrpcCall() {
        // @TODO: refactor
    }

    @Override
    public void closeGrpcCall() {
        // @TODO: refactor
    }

    @Override
    public void blockingUnaryCall(Object req, Object res) {
        // @TODO: refactor
    }

    @Override
    public void sendMessage(Object message) {
        // @TODO: refactor
    }

    @Override
    public void toStringUtf8(Object value) {
        // @TODO: refactor
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
     * @since 1.3.1
     */
    @Override
    public boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework,
                                     String className, String matchClassName, String methodName, String methodSign, boolean isStatic,
                                     int hookType) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();

            if (!isCollectAllowed(className, methodName, methodSign, hookType, true)) {
                return false;
            }

            if (HookType.SPRINGAPPLICATION.equals(hookType)) {
                SpringApplicationImpl.getWebApplicationContext(retValue);
            } else {
                MethodEvent event = new MethodEvent(0, -1, className, matchClassName, methodName,
                        methodSign, methodSign, instance, argumentArray, retValue, framework, isStatic, null);
                if (HookType.HTTP.equals(hookType)) {
                    HttpImpl.solveHttp(event);
                } else if (HookType.RPC.equals(hookType)) {
                    solveRPC(framework, event);
                }
            }
        } catch (Exception e) {
            DongTaiLog.error("collect method pool failed: " + e.toString(), e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    private void solveRPC(String framework, MethodEvent event) {
        if ("dubbo".equals(framework)) {
            DubboHandler.solveDubbo(event, SpyDispatcherImpl.INVOKE_ID_SEQUENCER);
        }
    }

    @Override
    public boolean collectMethod(Object instance, Object[] parameters, Object retObject, String methodMatcher,
                                 String className, String matchedClassName, String methodName, String signature,
                                 boolean isStatic) {
        try {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().enterAgent();
            PolicyNode policyNode = getPolicyNode(methodMatcher);
            if (policyNode == null) {
                return false;
            }

            if (!isCollectAllowed(className, methodName, signature, policyNode.getType().getType(), false)) {
                return false;
            }

            MethodEvent event = new MethodEvent(0, -1, className, matchedClassName, methodName,
                    signature, signature, instance, parameters, retObject, "", isStatic, null);

            if ((policyNode instanceof SourceNode) && PolicyNodeType.SOURCE.equals(policyNode.getType())) {
                SourceImpl.solveSource(event, (SourceNode) policyNode, INVOKE_ID_SEQUENCER);
                return true;
            } else if ((policyNode instanceof PropagatorNode) && PolicyNodeType.PROPAGATOR.equals(policyNode.getType())) {
                PropagatorImpl.solvePropagator(event, (PropagatorNode) policyNode, INVOKE_ID_SEQUENCER);
                return true;
            } else if ((policyNode instanceof SinkNode) && PolicyNodeType.SINK.equals(policyNode.getType())) {
                SinkImpl.solveSink(event, (SinkNode) policyNode);
                return true;
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.error("collect method failed", e);
        } finally {
            ScopeManager.SCOPE_TRACKER.getPolicyScope().leaveAgent();
        }
        return false;
    }

    private boolean isCollectAllowed(String className, String methodName, String signature,
                                     int policyNodeType, boolean isEnterEntry) {
        if (!isEnterEntry) {
            if (!ScopeManager.SCOPE_TRACKER.inEnterEntry()) {
                return false;
            }
        }

        // check hook point fallback
        if (EngineManager.isHookPointFallback()) {
            return false;
        }

        // 尝试获取hook限速令牌, 耗尽时降级
        if (!EngineManager.getFallbackManager().getHookRateLimiter().acquire()) {
            EngineManager.openHookPointFallback(className, methodName, signature, policyNodeType);
            return false;
        }

        return true;
    }

    private PolicyNode getPolicyNode(String methodMatcher) {
        AgentEngine agentEngine = AgentEngine.getInstance();
        PolicyManager policyManager = agentEngine.getPolicyManager();
        if (policyManager == null) {
            return null;
        }
        Policy policy = policyManager.getPolicy();
        if (policy == null) {
            return null;
        }

        return policy.getPolicyNode(methodMatcher);
    }
}
