package com.secnium.iast.core.handler.vulscan.normal;

import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.Asserts;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CookieFlagsMissingVulScan extends AbstractNormalVulScan {
    @Override
    public void scan(IastSinkModel sink, MethodEvent event, AtomicInteger invokeId) {
        Asserts.NOT_NULL("sink.params.position", sink.getPos());
        Asserts.NOT_NULL("sink.params.value", event.argumentArray);

        int[] taintPos = sink.getPos();
        Object[] arguments = event.argumentArray;

        if (arguments.length >= taintPos.length) {
            for (Integer pos : taintPos) {
                if (null != arguments[pos]) {
                    Boolean flag = (Boolean) arguments[pos];
                    if (!flag) {
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
