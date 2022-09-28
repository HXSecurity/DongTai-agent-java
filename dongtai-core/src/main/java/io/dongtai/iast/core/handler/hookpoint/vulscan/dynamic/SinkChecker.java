package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;

public interface SinkChecker {
    boolean match(IastSinkModel sink);
}
