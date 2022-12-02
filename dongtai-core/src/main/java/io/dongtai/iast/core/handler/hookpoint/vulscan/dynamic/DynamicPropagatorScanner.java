package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.handler.hookpoint.vulscan.IVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe.XXECheck;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DynamicPropagatorScanner implements IVulScan {
    private final static String HTTP_CLIENT_5 = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)"
            .substring(1);
    private final static String HTTP_CLIENT_4 = " org.apache.commons.httpclient.HttpClient.executeMethod(org.apache.commons.httpclient.HostConfiguration,org.apache.commons.httpclient.HttpMethod,org.apache.commons.httpclient.HttpState)"
            .substring(1);

    private final static Set<SinkSafeChecker> SAFE_CHECKERS = new HashSet<SinkSafeChecker>(Arrays.asList(
            new FastjsonCheck(),
            new XXECheck()
    ));

    private final static Set<SinkSourceChecker> SOURCE_CHECKERS = new HashSet<SinkSourceChecker>(Arrays.asList(
            new PathTraversalCheck(),
            new SSRFSourceCheck(),
            new UnvalidatedRedirectCheck()
    ));

    @Override
    public void scan(MethodEvent event, SinkNode sinkNode) {
        // @TODO: add traceId header to outgoing http request

        for (SinkSafeChecker chk : SAFE_CHECKERS) {
            if (chk.match(event, sinkNode) && chk.isSafe(event, sinkNode)) {
                return;
            }
        }

        if (sinkSourceHitTaintPool(event, sinkNode)) {
            StackTraceElement[] stackTraceElements = StackUtils.createCallStack(5);
            if (sinkNode.hasDenyStack(stackTraceElements)) {
                return;
            }
            event.setCallStacks(stackTraceElements);
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
            event.setTaintPositions(sinkNode.getSources(), null);

            EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
        }
    }

    /**
     * sink方法的污点来源是否命中污点池，用于过滤未命中污点池的sink方法，避免浪费资源，设置污点源去向
     *
     * @param event    current method event
     * @param sinkNode current sink policy node
     * @return 当前方法是否命中污点池
     */
    private boolean sinkSourceHitTaintPool(MethodEvent event, SinkNode sinkNode) {
        for (SinkSourceChecker chk : SOURCE_CHECKERS) {
            if (chk.match(event, sinkNode)) {
                return chk.checkSource(event, sinkNode);
            }
        }

        List<Object> sourceInstances = new ArrayList<Object>();
        boolean hasTaint = false;
        boolean objHasTaint = false;
        Set<TaintPosition> sources = sinkNode.getSources();
        for (TaintPosition position : sources) {
            if (position.isObject()) {
                if (TaintPoolUtils.isNotEmpty(event.objectInstance)
                        && TaintPoolUtils.isAllowTaintType(event.objectInstance)
                        && TaintPoolUtils.poolContains(event.objectInstance, event)) {
                    objHasTaint = true;
                    hasTaint = true;
                    addSourceInstance(sourceInstances, event.objectInstance);
                }
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.parameterInstances.length) {
                    continue;
                }
                boolean paramHasTaint = false;
                Object parameter = event.parameterInstances[parameterIndex];
                if (TaintPoolUtils.isNotEmpty(parameter)
                        && TaintPoolUtils.isAllowTaintType(parameter)
                        && TaintPoolUtils.poolContains(parameter, event)) {
                    paramHasTaint = true;
                    hasTaint = true;
                    addSourceInstance(sourceInstances, parameter);
                }
                event.addParameterValue(parameterIndex, parameter, paramHasTaint);
            }
        }


        // TODO: check taint tags at server
        if (VulnType.REFLECTED_XSS.equals(sinkNode.getVulType()) && !sourceInstances.isEmpty()) {
            boolean tagsHit = false;
            for (Object sourceInstance : sourceInstances) {
                int hash = System.identityHashCode(sourceInstance);
                TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(hash);
                if (tr == null || tr.isEmpty()) {
                    continue;
                }
                TaintTag[] required = new TaintTag[]{
                        TaintTag.UNTRUSTED, TaintTag.CROSS_SITE
                };
                TaintTag[] disallowed = new TaintTag[]{
                        TaintTag.XSS_ENCODED, TaintTag.URL_ENCODED,
                        TaintTag.HTML_ENCODED, TaintTag.BASE64_ENCODED
                };
                if (tr.hasRequiredTaintTags(required) && !tr.hasDisallowedTaintTags(disallowed)) {
                    tagsHit = true;
                }
            }
            if (!tagsHit) {
                return false;
            }
        }

        if (hasTaint) {
            event.setObjectValue(event.objectInstance, objHasTaint);
        }

        return hasTaint;
    }

    private void addSourceInstance(List<Object> sourceInstances, Object obj) {
        if (obj instanceof String[]) {
            String[] stringArray = (String[]) obj;
            for (String stringItem : stringArray) {
                addSourceInstance(sourceInstances, stringItem);
            }
        } else if (obj instanceof Object[]) {
            Object[] objArray = (Object[]) obj;
            for (Object objItem : objArray) {
                addSourceInstance(sourceInstances, objItem);
            }
        } else {
            sourceInstances.add(obj);
        }
    }
}
