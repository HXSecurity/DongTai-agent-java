package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;

public interface ServiceTrace {
    boolean match(MethodEvent event, PolicyNode policyNode);

    void addTrace(MethodEvent event, PolicyNode policyNode);
}
