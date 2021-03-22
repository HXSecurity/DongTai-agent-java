package com.secnium.iast.core;

import com.secnium.iast.core.util.base64.Base64Utils;
import org.junit.Test;

public class Base64EncodeTest {
    @Test
    public void encode() {
        String data = "alskdjflkasjdflkajsdlfkj";
        String encryptedData1 = Base64Utils.encodeBase64String(data.getBytes());
        System.out.println("encryptedData1 = " + encryptedData1);
    }
}
