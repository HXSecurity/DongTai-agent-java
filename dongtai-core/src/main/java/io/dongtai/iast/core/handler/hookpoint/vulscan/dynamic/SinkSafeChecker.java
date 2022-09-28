package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

public interface SinkSafeChecker extends SinkChecker {
    boolean isSafe(MethodEvent event, IastSinkModel sink);
}
