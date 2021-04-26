package com.secnium.iast.core.handler.vulscan;

import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.models.IastSinkModel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义漏洞扫描的接口
 *
 * @author dongzhiyong@huoxian.cn
 */
public interface IVulScan {
    /**
     * 执行扫描
     *
     * @param sink              当前命中的sink点
     * @param event             当前命中的方法
     * @param invokeIdSequencer 方法调用ID生成器，确保全局唯一且自增
     */
    void scan(IastSinkModel sink, MethodEvent event, AtomicInteger invokeIdSequencer);

    /**
     * 执行sql语句扫描
     *
     * @param sql 待扫描的sql语句
     * @param params sql语句对应的查询参数
     */
    void scan(String sql, Object[] params);
}
