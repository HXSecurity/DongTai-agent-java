package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.policy.ValidatorNode;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRange;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRanges;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.TaintRangesBuilder;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.dongtai.iast.core.utils.TaintPoolUtils.getStringHash;

public class ValidatorImpl {

    /**
     * 处理 Validator 点的事件
     *
     * @param event Validator 点事件
     */
    public static void solveValidator(MethodEvent event, ValidatorNode validatorNode, AtomicInteger invokeIdSequencer) {
        if (EngineManager.TAINT_HASH_CODES.isEmpty()) {
            return;
        }
        Set<TaintPosition> sources = validatorNode.getSources();
        if (sources.isEmpty()) {
            return;
        }

        for (TaintPosition position : sources) {
            Long hash = null;
            Integer len = null;
            if (position.isObject()) {
                if (TaintPoolUtils.isNotEmpty(event.objectInstance)
                        && TaintPoolUtils.isAllowTaintType(event.objectInstance)
                        && TaintPoolUtils.poolContains(event.objectInstance, event)) {
                    hash = getStringHash(event.objectInstance);
                    len = TaintRangesBuilder.getLength(event.objectInstance);
                    event.setObjectValue(event.objectInstance, true);
                }
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.parameterInstances.length) {
                    continue;
                }
                Object parameter = event.parameterInstances[parameterIndex];
                if (TaintPoolUtils.isNotEmpty(parameter)
                        && TaintPoolUtils.isAllowTaintType(parameter)
                        && TaintPoolUtils.poolContains(parameter, event)) {
                    hash = getStringHash(parameter);
                    len = TaintRangesBuilder.getLength(parameter);
                    event.addParameterValue(parameterIndex, parameter, true);
                }
            } else return;

            if (null != len && null != hash){
                TaintRanges tr = new TaintRanges(new TaintRange(TaintTag.VALIDATED.getKey(), 0, len));
                if (validatorNode.hasTags()) {
                    String[] tags = validatorNode.getTags();
                    for (String tag : tags) {
                        tr.add(new TaintRange(tag, 0, len));
                    }
                }
                event.sourceRanges.add(new MethodEvent.MethodEventTargetRange(hash, tr));
                TaintRanges taintRanges = EngineManager.TAINT_RANGES_POOL.get().get(hash);
                if (null == taintRanges){
                    EngineManager.TAINT_RANGES_POOL.add(hash, tr);
                }else {
                    taintRanges.addAll(tr);
                }
            }else return;
        }

        event.source = false;
        event.setCallStacks(StackUtils.createCallStack(4));
        event.setTaintPositions(validatorNode.getSources(), null);

        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.setPolicyType(PolicyNodeType.VALIDATOR.getName());
        EngineManager.TRACK_MAP.addTrackMethod(invokeId, event);
    }

}
