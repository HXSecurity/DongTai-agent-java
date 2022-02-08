package com.secnium.iast.core.handler.vulscan;

import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;

/**
 * 定义漏洞扫描的接口
 *
 * @author dongzhiyong@huoxian.cn
 */
public interface IVulScan {
    /**
     * 执行扫描
     *
     * @param sink  当前命中的sink点
     * @param event 当前命中的方法
     */
    void scan(IastSinkModel sink, MethodEvent event);

    /**
     * 执行sql语句扫描
     *
     * @param sql    待扫描的sql语句
     * @param params sql语句对应的查询参数
     */
    void scan(String sql, Object[] params);
}
