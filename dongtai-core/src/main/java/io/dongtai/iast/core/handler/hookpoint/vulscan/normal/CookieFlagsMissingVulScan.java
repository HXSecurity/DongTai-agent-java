package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.log.DongTaiLog;

import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CookieFlagsMissingVulScan extends AbstractNormalVulScan {
    @Override
    public void scan(MethodEvent event, SinkNode sinkNode) {
        Set<TaintPosition> sources = sinkNode.getSources();
        Object[] arguments = event.parameterInstances;
        if (!TaintPosition.hasParameter(sources)) {
            return;
        }

        for (TaintPosition position : sources) {
            try {
                if (position.isObject() || position.isReturn()) {
                    continue;
                }
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= arguments.length) {
                    continue;
                }

                Boolean flag = (Boolean) arguments[parameterIndex];
                if (flag) {
                    continue;
                }
                sendReport(getLatestStack(), sinkNode.getVulType());
                break;
            } catch (Throwable e) {
                DongTaiLog.trace("CookieFlagsMissingVulScan scan failed: {}, {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            }
        }
    }
}
