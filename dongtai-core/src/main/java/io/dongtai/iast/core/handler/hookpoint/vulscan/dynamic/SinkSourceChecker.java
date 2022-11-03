package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;

public interface SinkSourceChecker extends SinkChecker {
    boolean checkSource(MethodEvent event, SinkNode sinkNode);
}
