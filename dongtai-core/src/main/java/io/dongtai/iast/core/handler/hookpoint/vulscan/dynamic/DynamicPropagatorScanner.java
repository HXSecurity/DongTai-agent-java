package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.SpyDispatcherImpl;
import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.IVulScan;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DynamicPropagatorScanner implements IVulScan {

    private static String REDIRECT_METHOD_NAME = "location";
    private static String REDIRECT_LOWER_METHOD_NAME = "Location";
    private static String UNVALIDATED_REDIRECT = "unvalidated-redirect";
    private static HashSet<String> SIGNATURES = new HashSet<String>(Arrays.asList(
            " javax.servlet.http.HttpServletResponse.setHeader(java.lang.String,java.lang.String)".substring(1),
            " javax.servlet.http.HttpServletResponse.addHeader(java.lang.String,java.lang.String)".substring(1),
            " io.netty.handler.codec.http.DefaultHttpHeaders.add0(int,int,java.lang.CharSequence,java.lang.CharSequence)"
                    .substring(1)
    ));
    private static String HTTP_CLIENT_5 = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)"
            .substring(1);
    private static String HTTP_CLIENT_4 = " org.apache.commons.httpclient.HttpClient.executeMethod(org.apache.commons.httpclient.HostConfiguration,org.apache.commons.httpclient.HttpMethod,org.apache.commons.httpclient.HttpState)"
            .substring(1);

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

        if (sinkSourceHitTaintPool(event, sink)) {
            event.setCallStacks(StackUtils.createCallStack(11));
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
        if (isRedirectVul(sink.getType(), event.signature)) {
            String attribute = String.valueOf(event.argumentArray[0]);
            DongTaiLog.debug("add Header method, attribute name is {} ", attribute);
            if (attributeIsLocation(attribute)) {
                Object attributeValue = event.argumentArray[1];
                hitTaintPool = TaintPoolUtils.poolContains(attributeValue, event);
            }
        } else {
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
                    event.inValue = sourceValue;
                }
            } else {
                hitTaintPool = TaintPoolUtils.poolContains(event.object, event);
                if (hitTaintPool) {
                    event.inValue = event.object;
                }
            }
        }
        return hitTaintPool;
    }

    /**
     * 检查是否为重定向方法调用
     *
     * @param vulType         sink点类型
     * @param methodSignature 方法签名
     * @return true，false
     */
    public static boolean isRedirectVul(String vulType, String methodSignature) {
        return UNVALIDATED_REDIRECT.equals(vulType) && SIGNATURES.contains(methodSignature);
    }

    /**
     * 检查addHeader方法中设置的属性名称是否为location/Location
     *
     * @param attribute 属性名
     * @return true，false 正常理解即可
     */
    private static boolean attributeIsLocation(String attribute) {
        return REDIRECT_METHOD_NAME.equals(attribute) || REDIRECT_LOWER_METHOD_NAME.equals(attribute);
    }

}
