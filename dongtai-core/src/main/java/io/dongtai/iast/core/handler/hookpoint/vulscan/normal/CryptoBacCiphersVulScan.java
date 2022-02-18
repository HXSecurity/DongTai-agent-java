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
public class CryptoBacCiphersVulScan extends AbstractNormalVulScan {
    private final static Pattern GOOD_CIPHERS = Pattern.compile("^(DESede|AES|RSA).*$", CASE_INSENSITIVE);

    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        int[] taintPos = sink.getPos();
        Object[] arguments = event.argumentArray;
        Asserts.NOT_NULL("sink.params.position", taintPos);
        Asserts.NOT_NULL("sink.params.value", arguments);

        Matcher matcher;
        for (int pos : taintPos) {
            try {
                matcher = GOOD_CIPHERS.matcher((CharSequence) arguments[pos]);
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
