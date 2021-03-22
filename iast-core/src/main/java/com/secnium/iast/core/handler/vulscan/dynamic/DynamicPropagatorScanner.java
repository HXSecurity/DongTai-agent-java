package com.secnium.iast.core.handler.vulscan.dynamic;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.controller.impl.SinkImpl;
import com.secnium.iast.core.handler.models.IASTSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.IVulScan;
import com.secnium.iast.core.util.StackUtils;
import com.secnium.iast.core.util.TaintPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final Logger logger = LoggerFactory.getLogger(SinkImpl.class);

    @Override
    public void scan(IASTSinkModel sink, MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (sinkSourceHitTaintPool(event, sink)) {
            checkVulnAndGenerateReport(event, sink, invokeIdSequencer);
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
     * sink方法的污点来源是否命中污点池，用于过滤未命中污点池的sink方法，避免浪费资源
     *
     * @param event 当前方法事件
     * @param sink  命中的sink点
     * @return 当前方法是否命中污点池
     */
    private boolean sinkSourceHitTaintPool(MethodEvent event, IASTSinkModel sink) {
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
                for (int index : taintPositionIndexArray) {
                    if (event.argumentArray.length > index) {
                        hitTaintPool = TaintPoolUtils.poolContains(event.argumentArray[index], event);
                        if (hitTaintPool) {
                            break;
                        }
                    }
                }
            } else {
                hitTaintPool = TaintPoolUtils.poolContains(event.object, event);
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
     * @param event             当前调用的方法事件
     * @param sink              当前命中的sink模型实例
     * @param invokeIdSequencer 检测引擎全局的序列号生成器，用于生成有序、唯一的的方法调用ID
     */
    private void checkVulnAndGenerateReport(MethodEvent event, IASTSinkModel sink, AtomicInteger invokeIdSequencer) {
        event.setCallStacks(StackUtils.createCallStack(11));
        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

}
