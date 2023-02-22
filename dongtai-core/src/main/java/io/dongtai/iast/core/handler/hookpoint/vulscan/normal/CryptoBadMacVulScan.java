package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.TaintPosition;
import io.dongtai.log.DongTaiLog;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CryptoBadMacVulScan extends AbstractNormalVulScan {
    private final static Pattern GOOD_MAC_PAT = Pattern.compile("^(SHA2|SHA-224|SHA-256|SHA3|SHA-384|SHA5|SHA512|SHA-512)$", CASE_INSENSITIVE);

    @Override
    public void scan(MethodEvent event, SinkNode sinkNode) {
        Set<TaintPosition> sources = sinkNode.getSources();
        Object[] arguments = event.parameterInstances;
        if (!TaintPosition.hasParameter(sources)) {
            return;
        }

        Matcher matcher;
        for (TaintPosition position : sources) {
            try {
                if (position.isObject() || position.isReturn()) {
                    continue;
                }
                int parameterIndex = position.getParameterIndex();
                if (parameterIndex >= arguments.length) {
                    continue;
                }

                matcher = GOOD_MAC_PAT.matcher((CharSequence) arguments[parameterIndex]);
                if (matcher.find()) {
                    continue;
                }
                StackTraceElement[] latestStack = getLatestStack();
                for (StackTraceElement stackTraceElement : latestStack) {
                    // 解决 java.security.SecureRandom.getInstance 导致的 weak hash 误报
                    if (stackTraceElement.toString().startsWith("java.security.SecureRandom.getInstance")) {
                        return;
                    }
                }
                sendReport(latestStack, sinkNode.getVulType());
                break;
            } catch (Throwable e) {
                DongTaiLog.trace("CryptoBadMacVulScan scan failed: {}, {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            }
        }
    }
}
