package com.secnium.iast.core.handler.vulscan.overpower;

import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.IVulScan;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class OverPowerScanner implements IVulScan {
    /**
     * 执行扫描
     *
     * @param sink              当前命中的sink点
     * @param event             当前命中的方法
     * @param invokeIdSequencer 方法调用ID生成器，确保全局唯一且自增
     */
    @Override
    public void scan(IastSinkModel sink, MethodEvent event, AtomicInteger invokeIdSequencer) {

    }

    @Override
    public void scan(String sql, Object[] params) {
        // 检查sql语句是否与污点池有关
        // System.out.println("===>>> 越权检测入口 - sql：" + sql);
        // 检查参数是否与污点池有关
    }
}
