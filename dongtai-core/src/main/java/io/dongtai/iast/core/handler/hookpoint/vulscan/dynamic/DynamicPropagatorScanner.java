package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.*;
import io.dongtai.iast.core.handler.hookpoint.vulscan.IVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.xxe.XXECheck;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Method;
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
            new XXECheck(),
            new ReflectionInjectionCheck()
    ));

    private final static Set<SinkSourceChecker> SOURCE_CHECKERS = new HashSet<SinkSourceChecker>(Arrays.asList(
            new PathTraversalCheck(),
            new SSRFSourceCheck(),
            new UnvalidatedRedirectCheck()
    ));

    @Override
    public void scan(MethodEvent event, SinkNode sinkNode) {
        String policySignature = null;
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }
        // todo: 判断是否为 ssrf，如果是，增加 header 头
        if (HTTP_CLIENT_5.equals(policySignature)) {
            Object obj = event.argumentArray[1];
            try {
                Method method = obj.getClass().getMethod("addHeader", String.class, Object.class);
                method.invoke(obj, ContextManager.getHeaderKey(), ContextManager.getSegmentId());
            } catch (Exception e) {
                // fixme: solve exception
                DongTaiLog.error(e);
            }
        } else if (HTTP_CLIENT_4.equals(policySignature)) {
            Object obj = event.argumentArray[1];
            try {
                Method method = obj.getClass().getMethod("setRequestHeader", String.class, String.class);
                method.invoke(obj, ContextManager.getHeaderKey(), ContextManager.getSegmentId());
            } catch (Exception e) {
                // fixme: solve exception
                DongTaiLog.error(e);
            }
        }

        for (SinkSafeChecker chk : SAFE_CHECKERS) {
            if (chk.match(event, sinkNode) && chk.isSafe(event, sinkNode)) {
                return;
            }
        }

        if (sinkSourceHitTaintPool(event, sinkNode)) {
            event.setCallStacks(StackUtils.createCallStack(5));
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
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

        List<Object> inValues = new ArrayList<Object>();
        List<String> inValueStrings = new ArrayList<String>();

        Set<TaintPosition> sources = sinkNode.getSources();

        for (TaintPosition position : sources) {
            if (position.isObject()) {
                if (!TaintPoolUtils.poolContains(event.object, event)) {
                    continue;
                }
                inValues.add(event.object);
                inValueStrings.add(event.obj2String(event.object));
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.argumentArray.length
                        || !TaintPoolUtils.poolContains(event.argumentArray[parameterIndex], event)) {
                    continue;
                }
                inValues.add(event.argumentArray[parameterIndex]);
                inValueStrings.add(event.obj2String(event.argumentArray[parameterIndex]));
            }
        }

        if (inValues.isEmpty()) {
            return false;
        }

        event.setInValue(inValues.toArray(), inValueStrings.toString());
        return true;
    }
}
