package com.secnium.iast.core.handler.vulscan.dynamic;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.EventListenerHandlers;
import com.secnium.iast.core.handler.controller.impl.SinkImpl;
import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.TaintPoolUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DynamicPropagatorScanner implements IVulScan {

    private static final String REDIRECT_METHOD_NAME = "location";
    private static final String REDIRECT_LOWER_METHOD_NAME = "Location";
    private static final String UNVALIDATED_REDIRECT = "unvalidated-redirect";
    private static final HashSet<String> SIGNATURES = new HashSet<String>(Arrays.asList(
            " javax.servlet.http.HttpServletResponse.setHeader(java.lang.String,java.lang.String)".substring(1),
            " javax.servlet.http.HttpServletResponse.addHeader(java.lang.String,java.lang.String)".substring(1),
            " io.netty.handler.codec.http.DefaultHttpHeaders.add0(int,int,java.lang.CharSequence,java.lang.CharSequence)".substring(1)
    ));

    private final Logger logger = LogUtils.getLogger(SinkImpl.class);

    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        if (sinkSourceHitTaintPool(event, sink)) {
            checkVulnAndGenerateReport(event);
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
        // 如果当前sink为 javax.servlet.http.HttpServletResponse.addHeader(java.lang.String,java.lang.String)，第一个参数是否为location时，漏洞类型为unvalidated-redirect时
        boolean hitTaintPool = false;
        if (isRedirectVuln(sink.getType(), event.signature)) {
            String attribute = String.valueOf(event.argumentArray[0]);
            logger.debug("add Header method, attribute name is {} ", attribute);
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
    public static boolean isRedirectVuln(String vulType, String methodSignature) {
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

    /**
     * 检查是否存在漏洞，如果存在，生成漏洞报告
     *
     * @param event 当前调用的方法事件
     */
    private void checkVulnAndGenerateReport(MethodEvent event) {
        event.setCallStacks(StackUtils.createCallStack(11));
        int invokeId = EventListenerHandlers.INVOKE_ID_SEQUENCER.getAndIncrement();
        event.setInvokeId(invokeId);
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

}
