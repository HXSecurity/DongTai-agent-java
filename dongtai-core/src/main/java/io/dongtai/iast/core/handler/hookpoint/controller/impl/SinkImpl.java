package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.IastHookRuleModel;
import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.DynamicPropagatorScanner;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.CookieFlagsMissingVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.CryptoBacCiphersVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.CryptoBadMacVulScan;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.CryptoWeakRandomnessVulScan;
import io.dongtai.iast.core.utils.Asserts;

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
