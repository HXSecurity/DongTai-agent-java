package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.models.IastHookRuleModel;
import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.VulnType;
import com.secnium.iast.core.handler.vulscan.dynamic.DynamicPropagatorScanner;
import com.secnium.iast.core.handler.vulscan.normal.CookieFlagsMissingVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoBacCiphersVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoBadMacVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoWeakRandomnessVulScan;
import com.secnium.iast.core.util.Asserts;

/**
 * 危险方法hook点处理方法
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SinkImpl {

    /**
     * 处理sink点的事件
     *
     * @param event sink点事件
     */
    public static void solveSink(MethodEvent event) {
        Asserts.NOT_NULL("method.event", event);
        IastSinkModel sink = IastHookRuleModel.getSinkByMethodSignature(event.signature);
        Asserts.NOT_NULL("sink", sink);

        String sinkType = sink.getType();
        if (VulnType.CRYPTO_WEEK_RANDOMNESS.equals(sinkType)) {
            new CryptoWeakRandomnessVulScan().scan(sink, event);
        } else if (VulnType.CRYPTO_BAD_MAC.equals(sinkType)) {
            new CryptoBadMacVulScan().scan(sink, event);
        } else if (VulnType.CRYPTO_BAC_CIPHERS.equals(sinkType)) {
            new CryptoBacCiphersVulScan().scan(sink, event);
        } else if (VulnType.COOKIE_FLAGS_MISSING.equals(sinkType)) {
            new CookieFlagsMissingVulScan().scan(sink, event);
        } else if (EngineManager.TAINT_POOL.isNotEmpty()) {
            new DynamicPropagatorScanner().scan(sink, event);
        }
    }


}
