package io.dongtai.iast.core.handler.hookpoint.vulscan.normal;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CryptoWeakRandomnessVulScan extends AbstractNormalVulScan {
    /**
     * 检查是否存在若随机数算法
     * fixme: 当出现如若随机数算法时，考虑如何列出出现若随机数算法的组件/平台/中间件，避免造成用户的困扰
     *
     * @param sink  当前命中的sink点
     * @param event 当前命中的方法
     */
    @Override
    public void scan(IastSinkModel sink, MethodEvent event) {
        // todo: 取调用栈信息
        sendReport(getLatestStack(), sink.getType());
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
