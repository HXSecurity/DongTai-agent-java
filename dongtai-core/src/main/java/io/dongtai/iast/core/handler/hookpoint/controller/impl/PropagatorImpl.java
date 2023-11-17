package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNodeType;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PropagatorNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.models.taint.range.*;
import io.dongtai.iast.core.handler.hookpoint.models.taint.tag.TaintTag;
import io.dongtai.iast.core.utils.StackUtils;
import io.dongtai.iast.core.utils.TaintPoolUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 传播节点处理逻辑暂无问题，后续优先排查其他地方的问题
 *
 * @author dongzhiyong@huoxian.cn
 */
public class PropagatorImpl {
    private final static Set<String> SKIP_SCOPE_METHODS = new HashSet<String>(Arrays.asList(
            "java.net.URI.<init>(java.lang.String)",
            "java.net.URI.<init>(java.lang.String,java.lang.String,java.lang.String)",
            "java.net.URI.<init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)",
            "java.net.URI.<init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String)", // indirect
            "java.net.URI.<init>(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String)",
            "java.net.URL.<init>(java.lang.String)", // indirect
            "java.net.URL.<init>(java.net.URL,java.lang.String)", // indirect
            "java.net.URL.<init>(java.net.URL,java.lang.String,java.net.URLStreamHandler)",
            "java.net.URL.<init>(java.lang.String,java.lang.String,java.lang.String)", // indirect
            "java.net.URL.<init>(java.lang.String,java.lang.String,int,java.lang.String)", // indirect
            "java.net.URL.<init>(java.lang.String,java.lang.String,int,java.lang.String,java.net.URLStreamHandler)",
            "com.fasterxml.jackson.databind.ObjectMapper.readValue(java.io.InputStream,com.fasterxml.jackson.databind.JavaType)",
            "com.baomidou.mybatisplus.core.override.MybatisMapperMethod.execute(org.apache.ibatis.session.SqlSession,java.lang.Object[])"
    ));

    public static void solvePropagator(MethodEvent event, PropagatorNode propagatorNode, AtomicInteger invokeIdSequencer) {
        if (EngineManager.TAINT_HASH_CODES.isEmpty()) {
            return;
        }
        auxiliaryPropagator(event, propagatorNode, invokeIdSequencer);

    }

    private static void addPropagator(PropagatorNode propagatorNode, MethodEvent event, AtomicInteger invokeIdSequencer) {
        // skip same source and target
        Set<TaintPosition> sources = propagatorNode.getSources();
        Set<TaintPosition> targets = propagatorNode.getTargets();

        TaintCommandRunner r = propagatorNode.getCommandRunner();
        // O => O || O => R, source equals target and no change in taint range
        if (event.getSourceHashes().equals(event.getTargetHashes())
                && sources.size() == 1 && targets.size() == 1
                && TaintPosition.hasObject(sources)
                && (r == null || TaintCommand.KEEP.equals(r.getCommand()) || TaintCommand.TRIM.equals(r.getCommand())
                || TaintCommand.TRIM_LEFT.equals(r.getCommand()) || TaintCommand.TRIM_RIGHT.equals(r.getCommand()))
        ) {
            if (TaintPosition.hasObject(targets) || TaintPosition.hasReturn(targets)) {
                return;
            }
        }

        event.source = false;
        event.setTaintPositions(propagatorNode.getSources(), propagatorNode.getTargets());
        event.setCallStacks(StackUtils.createCallStack(6));
        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        event.setPolicyType(PolicyNodeType.PROPAGATOR.getName());
        EngineManager.TRACK_MAP.get().put(invokeId, event);
    }

    private static void auxiliaryPropagator(MethodEvent event, PropagatorNode propagatorNode, AtomicInteger invokeIdSequencer) {
        Set<TaintPosition> sources = propagatorNode.getSources();
        if (sources.isEmpty() || propagatorNode.getTargets().isEmpty()) {
            return;
        }

        boolean hasTaint = false;
        for (TaintPosition position : sources) {
            if (position.isObject()) {
                boolean objHasTaint = false;
                if (TaintPoolUtils.isNotEmpty(event.objectInstance)
                        && TaintPoolUtils.isAllowTaintType(event.objectInstance)
                        && TaintPoolUtils.poolContains(event.objectInstance, event)) {
                    objHasTaint = true;
                    hasTaint = true;
                }
                event.setObjectValue(event.objectInstance, objHasTaint);
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
                }
                event.addParameterValue(parameterIndex, parameter, paramHasTaint);
            }
        }

        if (!hasTaint) {
            return;
        }
        boolean valid = setTarget(propagatorNode, event);
        if (!valid) {
            return;
        }

        if (!TaintPosition.hasObject(sources) && !TaintPosition.hasObject(propagatorNode.getTargets())) {
            event.setObjectValue(event.objectInstance, false);
        }

        addPropagator(propagatorNode, event, invokeIdSequencer);
    }

    private static boolean setTarget(PropagatorNode propagatorNode, MethodEvent event) {
        Set<TaintPosition> targets = propagatorNode.getTargets();
        if (targets == null || targets.isEmpty()) {
            return false;
        }

        boolean hasTaint = false;
        for (TaintPosition position : targets) {
            if (position.isObject()) {
                boolean objHasTaint = false;
                if (TaintPoolUtils.isNotEmpty(event.objectInstance)
                        && TaintPoolUtils.isAllowTaintType(event.objectInstance)) {
                    EngineManager.TAINT_HASH_CODES.addObject(event.objectInstance, event);
                    objHasTaint = true;
                    hasTaint = true;
                }
                event.setObjectValue(event.objectInstance, objHasTaint);
            } else if (position.isReturn()) {
                boolean retHasTaint = false;
                if (TaintPoolUtils.isNotEmpty(event.returnInstance)
                        && TaintPoolUtils.isAllowTaintType(event.returnInstance)) {
                    EngineManager.TAINT_HASH_CODES.addObject(event.returnInstance, event);
                    retHasTaint = true;
                    hasTaint = true;
                }
                event.setReturnValue(event.returnInstance, retHasTaint);
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.parameterInstances.length) {
                    continue;
                }
                Object parameter = event.parameterInstances[parameterIndex];
                if (TaintPoolUtils.isNotEmpty(parameter)
                        && TaintPoolUtils.isAllowTaintType(parameter)) {
                    EngineManager.TAINT_HASH_CODES.addObject(parameter, event);
                    event.addParameterValue(parameterIndex, parameter, true);
                    hasTaint = true;
                }
            }
        }

        if (hasTaint) {
            trackTaintRange(propagatorNode, event);
        }

        return hasTaint;
    }

    private static TaintRanges getTaintRanges(Object obj) {
        long hash = TaintPoolUtils.getStringHash(obj);
        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(hash);
        if (tr == null) {
            tr = new TaintRanges();
        } else {
            tr = tr.clone();
        }
        return tr;
    }

    private static void trackTaintRange(PropagatorNode propagatorNode, MethodEvent event) {
        TaintCommandRunner r = propagatorNode.getCommandRunner();

        TaintRanges oldTaintRanges = new TaintRanges();
        TaintRanges srcTaintRanges = new TaintRanges();

        Object src = null;
        Set<TaintPosition> sourceLocs = propagatorNode.getSources();
        if (sourceLocs.size() == 1 && TaintPosition.hasObject(sourceLocs)) {
            src = event.objectInstance;
            srcTaintRanges = getTaintRanges(src);
        } else if (sourceLocs.size() == 2 && TaintPosition.hasObject(sourceLocs)
                && TaintPosition.hasParameter(sourceLocs)) {
            oldTaintRanges = getTaintRanges(event.objectInstance);
            for (TaintPosition sourceLoc : sourceLocs) {
                if (sourceLoc.isParameter()) {
                    int parameterIndex = sourceLoc.getParameterIndex();
                    if (event.parameterInstances.length > parameterIndex) {
                        src = event.parameterInstances[parameterIndex];
                        srcTaintRanges = getTaintRanges(src);
                    }
                    break;
                }
            }
        } else if (sourceLocs.size() == 1 && TaintPosition.hasParameter(sourceLocs)) {
            for (TaintPosition sourceLoc : sourceLocs) {
                int parameterIndex = sourceLoc.getParameterIndex();
                if (event.parameterInstances.length > parameterIndex) {
                    src = event.parameterInstances[parameterIndex];
                    srcTaintRanges = getTaintRanges(src);
                }
            }
        }

        long tgtHash = 0;
        Object tgt = null;
        Set<TaintPosition> targetLocs = propagatorNode.getTargets();
        // may have multiple targets?
        if (targetLocs.size() > 1) {
            return;
        }
        if (TaintPosition.hasObject(targetLocs)) {
            tgt = event.objectInstance;
            tgtHash = TaintPoolUtils.getStringHash(tgt);
            oldTaintRanges = getTaintRanges(tgt);
        } else if (TaintPosition.hasReturn(targetLocs)) {
            tgt = event.returnInstance;
            tgtHash = TaintPoolUtils.getStringHash(tgt);
        } else if (TaintPosition.hasParameter(targetLocs)) {
            for (TaintPosition targetLoc : targetLocs) {
                int parameterIndex = targetLoc.getParameterIndex();
                if (event.parameterInstances.length > parameterIndex) {
                    tgt = event.parameterInstances[parameterIndex];
                    tgtHash = TaintPoolUtils.getStringHash(tgt);
                    oldTaintRanges = getTaintRanges(tgt);
                }
            }
        } else {
            // invalid policy
            return;
        }

        if (!TaintPoolUtils.isNotEmpty(tgt) || !TaintPoolUtils.isAllowTaintType(tgt) || tgtHash == 0) {
            return;
        }

        TaintRanges tr;
        if (r != null && src != null) {
            tr = r.run(propagatorNode, src, tgt, event.parameterInstances, oldTaintRanges, srcTaintRanges);
        } else {
            if (!srcTaintRanges.isEmpty() || !oldTaintRanges.isEmpty()) {
                int len = TaintRangesBuilder.getLength(tgt);
                tr = new TaintRanges(new TaintRange(0, len));

                Set<String> existsTags = new HashSet<String>();
                if (!oldTaintRanges.isEmpty()) {
                    for (TaintRange t1 : oldTaintRanges.getTaintRanges()) {
                        if (!TaintTag.UNTRUSTED.equals(t1.getName())) {
                            existsTags.add(t1.getName());
                        }
                    }
                }
                if (!srcTaintRanges.isEmpty()) {
                    for (TaintRange t2 : srcTaintRanges.getTaintRanges()) {
                        if (!TaintTag.UNTRUSTED.equals(t2.getName())) {
                            existsTags.add(t2.getName());
                        }
                    }
                }
                for (String t : existsTags) {
                    tr.add(new TaintRange(t, 0, len));
                }

                if (propagatorNode.hasTags()) {
                    String[] tags = propagatorNode.getTags();
                    for (String tag : tags) {
                        tr.add(new TaintRange(tag, 0, len));
                    }
                }
                tr.addAll(srcTaintRanges.explode(len));
                tr.addAll(oldTaintRanges);
                tr.merge();
                tr.untag(propagatorNode.getUntags());
            } else {
                tr = new TaintRanges();
            }
        }
        event.targetRanges.add(new MethodEvent.MethodEventTargetRange(tgtHash, tr));
        EngineManager.TAINT_RANGES_POOL.add(tgtHash, tr);
    }

    public static boolean isSkipScope(String signature) {
        return SKIP_SCOPE_METHODS.contains(signature);
    }
}
