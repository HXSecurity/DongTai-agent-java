package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.Asserts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CryptoBadMacVulScan extends AbstractNormalVulScan {
    private final static Pattern GOOD_MAC_PAT = Pattern.compile("^(SHA2|SHA-224|SHA-256|SHA3|SHA-384|SHA5|SHA512|SHA-512)$", CASE_INSENSITIVE);

    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        int[] taintPos = sink.getPos();
        Object[] arguments = event.argumentArray;
        Asserts.NOT_NULL("sink.mac.params", taintPos);
        Asserts.NOT_NULL("sink.mac.params", arguments);

        Matcher matcher;
        for (int pos : taintPos) {
            try {
                matcher = GOOD_MAC_PAT.matcher((CharSequence) arguments[pos]);
                if (matcher.find()) {
                    continue;
                }
                sendReport(getLatestStack(), sink.getType());
                break;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 执行sql语句扫描
     *
     * @param sql    待扫描的sql语句
     * @param params sql语句对应的查询参数
     */
    @Override
    public void scan(String sql, Object[] params) {

    }
}
