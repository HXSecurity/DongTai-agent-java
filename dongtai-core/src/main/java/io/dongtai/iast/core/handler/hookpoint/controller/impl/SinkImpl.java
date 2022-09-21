package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.*;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic.DynamicPropagatorScanner;
import io.dongtai.iast.core.handler.hookpoint.vulscan.normal.*;

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
        if (null == event){
            return;
        }
        IastSinkModel sink = IastHookRuleModel.getSinkByMethodSignature(event.signature);
        if (null == sink){
            return;
        }
        String sinkType = sink.getType();
        if (VulnType.CRYPTO_WEEK_RANDOMNESS.equals(sinkType)) {
            new CryptoWeakRandomnessVulScan().scan(sink, event);
        } else if (VulnType.CRYPTO_BAD_MAC.equals(sinkType)) {
            new CryptoBadMacVulScan().scan(sink, event);
        } else if (VulnType.CRYPTO_BAC_CIPHERS.equals(sinkType)) {
            new CryptoBacCiphersVulScan().scan(sink, event);
        } else if (VulnType.COOKIE_FLAGS_MISSING.equals(sinkType)) {
            new CookieFlagsMissingVulScan().scan(sink, event);
        } else if (!EngineManager.TAINT_HASH_CODES.isEmpty()) {
            new DynamicPropagatorScanner().scan(sink, event);
        }
    }


}
