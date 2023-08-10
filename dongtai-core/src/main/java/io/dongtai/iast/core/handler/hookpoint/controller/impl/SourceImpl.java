package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRangesBuilder;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 污点来源处理方法
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SourceImpl {

    /**
     * 属性黑名单，用于检测属性是否可用
     */
    private static final ArrayList<String> WHITE_ATTRIBUTES = new ArrayList<String>();
    private static final String METHOD_OF_GETATTRIBUTE = "getAttribute";

    public static void solveSource(MethodEvent event, SourceNode sourceNode, AtomicInteger invokeIdSequencer) {
        if (!TaintPoolUtils.isNotEmpty(event.returnInstance)
                || !TaintPoolUtils.isAllowTaintType(event.returnInstance)
                || !allowCall(event)) {
            return;
        }

        event.source = true;
        event.setCallStacks(StackUtils.createCallStack(4));

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.setPolicyType(PolicyNodeType.SOURCE.getName());

        boolean valid = trackTarget(event, sourceNode);
        if (!valid) {
            return;
        }

        for (TaintPosition src : sourceNode.getSources()) {
            if (src.isObject()) {
                event.setObjectValue(event.returnInstance, true);
            } else if (src.isParameter()) {
                if (event.parameterInstances.length > src.getParameterIndex()) {
                    event.addParameterValue(src.getParameterIndex(), event.parameterInstances[src.getParameterIndex()], true);
                }
            }
        }

        for (TaintPosition tgt : sourceNode.getTargets()) {
            if (tgt.isObject()) {
                event.setObjectValue(event.returnInstance, true);
            } else if (tgt.isParameter()) {
                if (event.parameterInstances.length > tgt.getParameterIndex()) {
                    event.addParameterValue(tgt.getParameterIndex(), event.parameterInstances[tgt.getParameterIndex()], true);
                }
            } else if (tgt.isReturn()) {
                event.setReturnValue(event.returnInstance, true);

            }
        }

        if (!TaintPosition.hasObject(sourceNode.getSources()) && !TaintPosition.hasObject(sourceNode.getTargets())) {
            event.setObjectValue(event.objectInstance, false);
        }

        event.setTaintPositions(sourceNode.getSources(), sourceNode.getTargets());

        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

    private static boolean trackTarget(MethodEvent event, SourceNode sourceNode) {
        int length = TaintRangesBuilder.getLength(event.returnInstance);
        if (length == 0) {
            return false;
        }

        TaintPoolUtils.trackObject(event, sourceNode, event.returnInstance, 0, false);
        return true;
    }

    private static boolean allowCall(MethodEvent event) {
        boolean allowed = true;
        if (METHOD_OF_GETATTRIBUTE.equals(event.getMethodName())) {
            return allowAttribute((String) event.parameterInstances[0]);
        }
        return allowed;
    }

    /**
     * 检查属性是否xxx，时间复杂度：O(n) fixme: spring参数解析，白名单导致数据不正确
     *
     * @param attribute 属性名称
     * @return true-属性允许，false-属性不允许
     */
    private static boolean allowAttribute(String attribute) {
        return WHITE_ATTRIBUTES.contains(attribute);
    }

    static {
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.bestMatchingPattern".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.uriTemplateVariables".substring(1));
        WHITE_ATTRIBUTES.add(" org.springframework.web.servlet.View.pathVariables".substring(1));
    }

}
