package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.handler.models.IASTSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.Asserts;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CryptoBacCiphersVulScan extends AbstractNormalVulScan {
    private final static Pattern GOOD_CIPHERS = Pattern.compile("^(DESede|AES|RSA).*$", CASE_INSENSITIVE);

    @Override
    public void scan(IASTSinkModel sink, MethodEvent event, AtomicInteger invokeId) {
        Asserts.NOT_NULL("sink.params.position", sink.getPos());
        Asserts.NOT_NULL("sink.params.value", event.argumentArray);

        int[] taintPos = sink.getPos();
        Object[] arguments = event.argumentArray;

        if (arguments.length >= taintPos.length) {
            Matcher matcher;
            for (Integer pos : taintPos) {
                if (null != arguments[pos]) {
                    String cipher = (String) event.argumentArray[pos];
                    matcher = GOOD_CIPHERS.matcher(cipher);

                    if (!matcher.find()) {
                        // todo: 获取调用栈信息
                        sendReport(getLatestStack(), sink.getType());
                        break;
                    }
                }
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
