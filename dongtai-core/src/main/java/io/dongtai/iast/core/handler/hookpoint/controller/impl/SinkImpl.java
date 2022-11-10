package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
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
    public static void solveSink(MethodEvent event, SinkNode sinkNode) {
        if (null == event) {
            return;
        }

        String vulType = sinkNode.getVulType();
        if (VulnType.CRYPTO_WEAK_RANDOMNESS.equals(vulType)) {
            new CryptoWeakRandomnessVulScan().scan(event, sinkNode);
        } else if (VulnType.CRYPTO_BAD_MAC.equals(vulType)) {
            new CryptoBadMacVulScan().scan(event, sinkNode);
        } else if (VulnType.CRYPTO_BAC_CIPHERS.equals(vulType)) {
            new CryptoBacCiphersVulScan().scan(event, sinkNode);
        } else if (VulnType.COOKIE_FLAGS_MISSING.equals(vulType)) {
            new CookieFlagsMissingVulScan().scan(event, sinkNode);
        } else if (!EngineManager.TAINT_HASH_CODES.isEmpty()) {
            new DynamicPropagatorScanner().scan(event, sinkNode);
        }
    }


}
