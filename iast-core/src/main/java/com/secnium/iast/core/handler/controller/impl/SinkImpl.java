package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.handler.models.IASTHookRuleModel;
import com.secnium.iast.core.handler.models.IASTSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.ScannerFactory;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;
import com.secnium.iast.core.util.Asserts;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 危险方法hook点处理方法
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SinkImpl {

    /**
     * 处理sink点的事件
     *
     * @param event             sink点事件
     * @param invokeIdSequencer 随机数序列
     */
    public static void solveSink(MethodEvent event, AtomicInteger invokeIdSequencer) {
        boolean setJdbcImpl = false;
        Asserts.NOT_NULL("method.event", event);
        IASTSinkModel sink = IASTHookRuleModel.getSinkByMethodSignature(event.signature);
        Asserts.NOT_NULL("sink", sink);
        IJdbc jdbcImpl = null;
        try {
            setJdbcImpl = ScannerFactory.preScan(sink, event, jdbcImpl);
            ScannerFactory.scan(invokeIdSequencer, sink, jdbcImpl, event);
        } finally {
            if (setJdbcImpl && jdbcImpl != null) {
                jdbcImpl.removeEvent();
            }
        }
    }


}
