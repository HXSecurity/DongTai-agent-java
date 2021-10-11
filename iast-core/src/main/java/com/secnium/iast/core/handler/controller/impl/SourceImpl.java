package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.StackUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private static final ArrayList<String> WIHTE_ATTRIBUTES = new ArrayList<String>();
    private static final String METHOD_OF_GETATTRIBUTE = "getAttribute";

    public static void solveSource(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (isNotEmpty(event.returnValue) && isAllowTaintType(event.returnValue) && allowCall(event)) {
            event.source = true;
            event.setCallStacks(StackUtils.createCallStack(9));

            int invokeId = invokeIdSequencer.getAndIncrement();
            event.setInvokeId(invokeId);
            event.inValue = event.argumentArray;
            event.outValue = event.returnValue;

            if (isNotEmpty(event.returnValue)) {
                EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
                EngineManager.TAINT_POOL.addTaintToPool(event.returnValue, event, true);
            }
        }
    }

    /**
     * 检查对象是否为空
     * - 集合类型，检查大小
     * - 字符串类型，检查是否为空字符串
     * - 其他情况，均认为非空
     *
     * @param obj 待检查的实例化对象
     * @return true-对象不为空；false-对象为空
     */
    private static boolean isNotEmpty(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Map) {
            Map<?, ?> taintValue = (Map<?, ?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof List) {
            List<?> taintValue = (List<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof Set) {
            Set<?> taintValue = (Set<?>) obj;
            return !taintValue.isEmpty();
        } else if (obj instanceof String) {
            String taintValue = (String) obj;
            return !taintValue.isEmpty();
        }
        return true;
    }

    private static boolean allowCall(MethodEvent event) {
        boolean allowed = true;
        if (METHOD_OF_GETATTRIBUTE.equals(event.getMethodName())) {
            return allowAttribute((String) event.argumentArray[0]);
        }
        return allowed;
    }

    /**
     * 检查属性是否xxx，时间复杂度：O(n)
     * fixme: spring参数解析，白名单导致数据不正确
     *
     * @param attribute 属性名称
     * @return true-属性允许，false-属性不允许
     */
    private static boolean allowAttribute(String attribute) {
        return WIHTE_ATTRIBUTES.contains(attribute);
    }


    public static boolean isAllowTaintType(Object obj) {
        return !(obj instanceof Boolean || obj instanceof Integer);
    }

    static {
        WIHTE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.bestMatchingPattern".substring(1));
        WIHTE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping".substring(1));
        WIHTE_ATTRIBUTES.add(" org.springframework.web.servlet.HandlerMapping.uriTemplateVariables".substring(1));
        WIHTE_ATTRIBUTES.add(" org.springframework.web.servlet.View.pathVariables".substring(1));
    }

}
