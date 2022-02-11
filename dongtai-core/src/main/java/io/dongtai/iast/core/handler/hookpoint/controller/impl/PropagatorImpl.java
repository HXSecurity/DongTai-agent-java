package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.IastHookRuleModel;
import io.dongtai.iast.core.handler.hookpoint.models.IastPropagatorModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.TrackUtils;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.dongtai.iast.core.utils.HashCode.isNotEmpty;

/**
 * 传播节点处理逻辑暂无问题，后续优先排查其他地方的问题
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PropagatorImpl {
    private static final String PARAMS_OBJECT = "O";
    private static final String PARAMS_PARAM = "P";
    private static final String PARAMS_RETURN = "R";
    private static final String CONDITION_AND = "&";
    private static final String CONDITION_OR = "|";
    private static final String CONDITION_AND_RE_PATTERN = "[\\|&]";
    private static final int STACK_DEPTH = 11;

    public static void solvePropagator(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (!EngineManager.TAINT_POOL.get().isEmpty()) {
            IastPropagatorModel propagator = IastHookRuleModel.getPropagatorByMethodSignature(event.signature);
            if (propagator != null) {
                auxiliaryPropagator(propagator, invokeIdSequencer, event);
            } else {
                autoPropagator(invokeIdSequencer, event);
            }
        }
    }

    private static void addPropagator(MethodEvent event, AtomicInteger invokeIdSequencer) {
        event.source = false;
        event.setCallStacks(StackUtils.createCallStack(STACK_DEPTH));
        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        EngineManager.TRACK_MAP.get().put(invokeId, event);
    }

    private static void auxiliaryPropagator(IastPropagatorModel propagator, AtomicInteger invokeIdSequencer, MethodEvent event) {
        String sourceString = propagator.getSource();
        boolean conditionSource = containts(sourceString);
        if (!conditionSource) {
            if (PARAMS_OBJECT.equals(sourceString) && isNotEmpty(event.object) && TaintPoolUtils.poolContains(event.object, event)) {
                event.inValue = event.object;
                setTarget(propagator, event);
                addPropagator(event, invokeIdSequencer);
            } else if (sourceString.startsWith(PARAMS_PARAM)) {
                ArrayList<Object> inValues = new ArrayList<Object>();
                int[] positions = (int[]) propagator.getSourcePosition();
                for (int pos : positions) {
                    if (pos < event.argumentArray.length) {
                        Object tempObj = event.argumentArray[pos];
                        if (isNotEmpty(tempObj) && TaintPoolUtils.poolContains(tempObj, event)) {
                            inValues.add(tempObj);
                        }
                    }

                }
                if (!inValues.isEmpty()) {
                    event.inValue = inValues.toArray();
                    setTarget(propagator, event);
                    addPropagator(event, invokeIdSequencer);
                }
            }
        } else {
            // o&r 解决
            boolean andCondition = sourceString.contains(CONDITION_AND);
            String[] conditionSources = sourceString.split(CONDITION_AND_RE_PATTERN);
            ArrayList<Object> inValues = new ArrayList<Object>();
            for (String source : conditionSources) {
                if (PARAMS_OBJECT.equals(source)) {
                    if (event.object == null) {
                        break;
                    }
                    inValues.add(event.object);
                } else if (PARAMS_RETURN.equals(source)) {
                    if (event.returnValue == null) {
                        break;
                    }
                    inValues.add(event.returnValue);
                } else if (source.startsWith(PARAMS_PARAM)) {
                    int[] positions = (int[]) propagator.getSourcePosition();
                    for (int pos : positions) {
                        Object tempObj = event.argumentArray[pos];
                        if (tempObj != null) {
                            inValues.add(tempObj);
                        }
                    }
                }
            }
            if (!inValues.isEmpty()) {
                int condition = 0;
                for (Object obj : inValues) {
                    if (isNotEmpty(obj) && TaintPoolUtils.poolContains(obj, event)) {
                        condition++;
                    }
                }
                if (condition > 0 && (!andCondition || conditionSources.length == condition)) {
                    event.inValue = inValues.toArray();
                    setTarget(propagator, event);
                    addPropagator(event, invokeIdSequencer);
                }
            }
        }
    }

    private static void setTarget(IastPropagatorModel propagator, MethodEvent event) {
        String target = propagator.getTarget();
        if (PARAMS_OBJECT.equals(target)) {
            event.outValue = event.object;
        } else if (PARAMS_RETURN.equals(target)) {
            event.outValue = event.returnValue;
        } else if (target.startsWith(PARAMS_PARAM)) {
            ArrayList<Object> outValues = new ArrayList<Object>();
            Object tempPositions = propagator.getTargetPosition();
            int[] positions = (int[]) tempPositions;
            if (positions.length == 1) {
                event.outValue = event.argumentArray[positions[0]];
            } else {
                for (int pos : positions) {
                    outValues.add(event.argumentArray[pos]);
                }
                if (!outValues.isEmpty()) {
                    event.outValue = outValues.toArray();
                }
            }
        }
        if (isNotEmpty(event.outValue)) {
            EngineManager.TAINT_POOL.addTaintToPool(event.outValue, event, false);
        }
    }

    private static void autoPropagator(AtomicInteger invokeIdSequence, MethodEvent event) {
        // 处理自动传播问题
        // 检查污点池，判断是否存在命中的污点
        Set<Object> pools = EngineManager.TAINT_POOL.get();
        for (Object taintValue : pools) {
            if (TrackUtils.smartEventMatchAndSetTaint(taintValue, event)) {
                // 将event.outValue加入污点池
                break;
                // 将当前方法加入污点方法池
            }
        }
        if (isNotEmpty(event.outValue)) {
            pools.add(event.outValue);
            if (event.outValue instanceof String) {
                event.addTargetHash(System.identityHashCode(event.outValue));
            } else {
                event.addTargetHash(event.outValue.hashCode());
            }
            addPropagator(event, invokeIdSequence);
        }
    }

    private static boolean containts(String obj) {
        return obj.contains(CONDITION_AND) || obj.contains(CONDITION_OR);
    }
}
