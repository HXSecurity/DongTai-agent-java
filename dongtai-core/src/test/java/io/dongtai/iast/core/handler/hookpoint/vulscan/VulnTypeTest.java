package io.dongtai.iast.core.handler.hookpoint.vulscan;

import org.junit.Test;


public class VulnTypeTest {
    @Test
    public void createVulType() {
        String name = "";
        VulnType vType = VulnType.getTypeByName("crypto-bad-mac");
        assert vType == VulnType.CRYPTO_BAD_MAC;
//        assert vType == VulnType.COOKIE_FLAGS_MISSING;
    }
}
