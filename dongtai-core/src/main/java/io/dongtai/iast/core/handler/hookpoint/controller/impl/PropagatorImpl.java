package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PropagatorNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange.*;
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
            "java.net.URL.<init>(java.lang.String,java.lang.String,int,java.lang.String,java.net.URLStreamHandler)"
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

        if (event.getSourceHashes().size() == event.getTargetHashes().size()
                && event.getSourceHashes().equals(event.getTargetHashes())
                && !(sources.size() == 1 && TaintPosition.hasObject(sources)
                && targets.size() == 1 && TaintPosition.hasObject(targets))
        ) {
            return;
        }

        event.source = false;
        event.setCallStacks(StackUtils.createCallStack(6));
        int invokeId = invokeIdSequencer.getAndIncrement();
        event.setInvokeId(invokeId);
        EngineManager.TRACK_MAP.get().put(invokeId, event);
    }

    private static void auxiliaryPropagator(MethodEvent event, PropagatorNode propagatorNode, AtomicInteger invokeIdSequencer) {
        Set<TaintPosition> sources = propagatorNode.getSources();
        if (sources.isEmpty() || propagatorNode.getTargets().isEmpty()) {
            return;
        }

        List<Object> inValues = new ArrayList<Object>();
        List<String> inValueStrings = new ArrayList<String>();
        for (TaintPosition position : sources) {
            if (position.isObject()) {
                if (!TaintPoolUtils.isNotEmpty(event.object)
                        || !TaintPoolUtils.isAllowTaintType(event.object)
                        || !TaintPoolUtils.poolContains(event.object, event)) {
                    continue;
                }

                inValues.add(event.object);
                inValueStrings.add(event.obj2String(event.object));
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= event.argumentArray.length) {
                    continue;
                }

                Object tempObj = event.argumentArray[parameterIndex];
                if (!TaintPoolUtils.isNotEmpty(tempObj)
                        || !TaintPoolUtils.isAllowTaintType(tempObj)
                        || !TaintPoolUtils.poolContains(tempObj, event)) {
                    continue;
                }
                inValues.add(tempObj);
                inValueStrings.add(event.obj2String(tempObj));
            }
        }

        if (!inValues.isEmpty()) {
            event.setInValue(inValues.toArray(), inValueStrings.toString());
            setTarget(propagatorNode, event);
            addPropagator(propagatorNode, event, invokeIdSequencer);
        }
    }

    private static void setTarget(PropagatorNode propagatorNode, MethodEvent event) {
        Set<TaintPosition> targets = propagatorNode.getTargets();
        if (targets == null || targets.isEmpty()) {
            return;
        }

        List<Object> outValues = new ArrayList<Object>();
        List<String> outValueStrings = new ArrayList<String>();
        for (TaintPosition position : targets) {
            if (position.isObject()) {
                outValues.add(event.object);
                if (targets.size() > 1) {
                    outValueStrings.add(event.obj2String(event.object));
                }
                trackTaintRange(propagatorNode, event);
            } else if (position.isReturn()) {
                outValues.add(event.returnValue);
                if (targets.size() > 1) {
                    outValueStrings.add(event.obj2String(event.returnValue));
                }
                trackTaintRange(propagatorNode, event);
            } else if (position.isParameter()) {
                int parameterIndex = position.getParameterIndex();
                if (event.argumentArray.length > parameterIndex) {
                    outValues.add(event.argumentArray[parameterIndex]);
                    if (targets.size() > 1) {
                        outValueStrings.add(event.obj2String(event.argumentArray[parameterIndex]));
                    }
                    trackTaintRange(propagatorNode, event);
                }
            }
        }

        if (outValues.isEmpty()) {
            return;
        } else if (outValues.size() == 1) {
            event.setOutValue(outValues.get(0));
        } else {
            event.setOutValue(outValues.toArray(), outValueStrings.toString());
        }

        EngineManager.TAINT_HASH_CODES.addObject(event.outValue, event, false);
    }

    private static TaintRanges getTaintRanges(Object obj) {
        int hash = System.identityHashCode(obj);
        TaintRanges tr = EngineManager.TAINT_RANGES_POOL.get(hash);
        if (tr == null) {
            tr = new TaintRanges();
        } else {
            tr = tr.clone();
        }
        return tr;
    }

    private static void trackTaintRange(PropagatorNode propagatorNode, MethodEvent event) {
        TaintCommandRunner r = TaintCommandRunner.getCommandRunner(event.signature);

        TaintRanges oldTaintRanges = new TaintRanges();
        TaintRanges srcTaintRanges = new TaintRanges();

        Object src = null;
        if (r != null) {
            Set<TaintPosition> sourceLocs = propagatorNode.getSources();
            if (sourceLocs.size() == 1 && TaintPosition.hasObject(sourceLocs)) {
                src = event.object;
                srcTaintRanges = getTaintRanges(src);
            } else if (sourceLocs.size() == 2 && TaintPosition.hasObject(sourceLocs)
                    && TaintPosition.hasParameter(sourceLocs)) {
                oldTaintRanges = getTaintRanges(event.object);
                for (TaintPosition sourceLoc : sourceLocs) {
                    if (sourceLoc.isParameter()) {
                        int parameterIndex = sourceLoc.getParameterIndex();
                        if (event.argumentArray.length > parameterIndex) {
                            src = event.argumentArray[parameterIndex];
                            srcTaintRanges = getTaintRanges(src);
                        }
                        break;
                    }
                }
            } else if (sourceLocs.size() == 1 && TaintPosition.hasParameter(sourceLocs)) {
                for (TaintPosition sourceLoc : sourceLocs) {
                    int parameterIndex = sourceLoc.getParameterIndex();
                    if (event.argumentArray.length > parameterIndex) {
                        src = event.argumentArray[parameterIndex];
                        srcTaintRanges = getTaintRanges(src);
                    }
                }
            }
        }

        int tgtHash = 0;
        Object tgt = null;
        Set<TaintPosition> targetLocs = propagatorNode.getTargets();
        if (targetLocs.size() == 1 && TaintPosition.hasObject(targetLocs)) {
            tgt = event.object;
            tgtHash = System.identityHashCode(tgt);
            oldTaintRanges = getTaintRanges(tgt);
        } else if (targetLocs.size() == 1 && TaintPosition.hasReturn(targetLocs)) {
            tgt = event.returnValue;
            tgtHash = System.identityHashCode(tgt);
        } else if (targetLocs.size() == 1 && TaintPosition.hasParameter(targetLocs)) {
            for (TaintPosition targetLoc : targetLocs) {
                int parameterIndex = targetLoc.getParameterIndex();
                if (event.argumentArray.length > parameterIndex) {
                    tgt = event.argumentArray[parameterIndex];
                    tgtHash = System.identityHashCode(tgt);
                    oldTaintRanges = getTaintRanges(tgt);
                }
            }
        } else {
            // invalid policy
            return;
        }

        if (!TaintPoolUtils.isNotEmpty(tgt) || tgtHash == 0) {
            return;
        }

        TaintRanges tr;
        if (r != null && src != null) {
            tr = r.run(src, tgt, event.argumentArray, oldTaintRanges, srcTaintRanges);
        } else {
            tr = new TaintRanges(new TaintRange(0, TaintRangesBuilder.getLength(tgt)));
        }
        event.targetRanges.add(new MethodEvent.MethodEventTargetRange(tgtHash, tr));
        EngineManager.TAINT_RANGES_POOL.add(tgtHash, tr);
    }

    public static boolean isSkipScope(String signature) {
        return SKIP_SCOPE_METHODS.contains(signature);
    }
}
