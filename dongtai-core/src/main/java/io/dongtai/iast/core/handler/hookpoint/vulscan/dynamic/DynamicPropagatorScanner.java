package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
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
            new XXECheck()
    ));

    private final static Set<SinkSourceChecker> SOURCE_CHECKERS = new HashSet<SinkSourceChecker>(Arrays.asList(
            new PathTraversalCheck(),
            new SSRFSourceCheck(),
            new UnvalidatedRedirectCheck()
    ));

    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        // todo: 判断是否为 ssrf，如果是，增加 header 头
        if (sink.getSignature().equals(HTTP_CLIENT_5)) {
            Object obj = event.argumentArray[1];
            try {
                Method method = obj.getClass().getMethod("addHeader", String.class, Object.class);
                method.invoke(obj, ContextManager.getHeaderKey(), ContextManager.getSegmentId());
            } catch (Exception e) {
                // fixme: solve exception
                DongTaiLog.error(e);
            }
        } else if (sink.getSignature().equals(HTTP_CLIENT_4)) {
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
            if (chk.match(sink) && chk.isSafe(event, sink)) {
                return;
            }
        }

        if (sinkSourceHitTaintPool(event, sink)) {
            event.setCallStacks(StackUtils.createCallStack(5));
            int invokeId = SpyDispatcherImpl.INVOKE_ID_SEQUENCER.getAndIncrement();
            event.setInvokeId(invokeId);
            EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
        }
    }

    /**
     * 执行sql语句扫描
     *
     * @param sql    待扫描的sql语句
     * @param params sql语句对应的查询参数
     */
    @Override
    public void scan(String sql, Object[] params) {

    }

    /**
     * sink方法的污点来源是否命中污点池，用于过滤未命中污点池的sink方法，避免浪费资源，设置污点源去向
     *
     * @param event 当前方法事件
     * @param sink  命中的sink点
     * @return 当前方法是否命中污点池
     */
    private boolean sinkSourceHitTaintPool(MethodEvent event, IastSinkModel sink) {
        boolean hitTaintPool = false;

        for (SinkSourceChecker chk : SOURCE_CHECKERS) {
            if (chk.match(sink)) {
                return chk.checkSource(event, sink);
            }
        }

        int[] taintPositionIndexArray = sink.getPos();

        if (taintPositionIndexArray != null) {
            Object sourceValue = null;
            for (int index : taintPositionIndexArray) {
                if (event.argumentArray.length > index) {
                    hitTaintPool = TaintPoolUtils.poolContains(event.argumentArray[index], event);
                    if (hitTaintPool) {
                        sourceValue = event.argumentArray[index];
                        break;
                    }
                }
            }
            if (hitTaintPool) {
                event.setInValue(sourceValue);
            }
        } else {
            hitTaintPool = TaintPoolUtils.poolContains(event.object, event);
            if (hitTaintPool) {
                event.setInValue(event.object);
            }
        }
        return hitTaintPool;
    }
}
