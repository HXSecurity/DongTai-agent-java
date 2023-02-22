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
public class CryptoBacCiphersVulScan extends AbstractNormalVulScan {
    private final static Pattern GOOD_CIPHERS = Pattern.compile("^(DESede|AES|RSA).*$", CASE_INSENSITIVE);

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

                matcher = GOOD_CIPHERS.matcher((CharSequence) arguments[parameterIndex]);
                if (matcher.find()) {
                    continue;
                }
                sendReport(getLatestStack(), sinkNode.getVulType());
                break;
            } catch (Throwable e) {
                DongTaiLog.trace("CryptoBacCiphersVulScan scan failed: {}, {}",
                        e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            }
        }
    }
}
