package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.Asserts;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CookieFlagsMissingVulScan extends AbstractNormalVulScan {
    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        int[] taintPos = sink.getPos();
        Object[] arguments = event.argumentArray;
        Asserts.NOT_NULL("sink.params.position", sink.getPos());
        Asserts.NOT_NULL("sink.params.value", event.argumentArray);

        for (int pos : taintPos) {
            try {
                Boolean flag = (Boolean) arguments[pos];
                if (flag) {
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
