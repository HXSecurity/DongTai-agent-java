package com.secnium.iast.core;

import io.dongtai.iast.core.utils.base64.Base64Decoder;
import io.dongtai.iast.core.utils.base64.Base64Encoder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class Base64EncodeTest {
    @Test
    public void encode() {
        String data = "alskdjflkasjdflkajsdlfkj";
        String encryptedData1 = Base64Encoder.encodeBase64String(data.getBytes());
        System.out.println("encryptedData1 = " + encryptedData1);

        encryptedData1 = "aG9zdDpsb2NhbGhvc3Q6ODA4MApjb25uZWN0aW9uOmtlZXAtYWxpdmUKY29udGVudC1sZW5ndGg6MTYKcHJhZ21hOm5vLWNhY2hlCmNhY2hlLWNvbnRyb2w6bm8tY2FjaGUKc2VjLWNoLXVhOiIgTm90O0EgQnJhbmQiO3Y9Ijk5IiwgIkdvb2dsZSBDaHJvbWUiO3Y9IjkxIiwgIkNocm9taXVtIjt2PSI5MSIKYWNjZXB0OiovKgp4LXJlcXVlc3RlZC13aXRoOlhNTEh0dHBSZXF1ZXN0CnNlYy1jaC11YS1tb2JpbGU6PzAKdXNlci1hZ2VudDpNb3ppbGxhLzUuMCAoTWFjaW50b3NoOyBJbnRlbCBNYWMgT1MgWCAxMF8xNV83KSBBcHBsZVdlYktpdC81MzcuMzYgKEtIVE1MLCBsaWtlIEdlY2tvKSBDaHJvbWUvOTEuMC40NDcyLjExNCBTYWZhcmkvNTM3LjM2CmNvbnRlbnQtdHlwZTphcHBsaWNhdGlvbi9qc29uOyBjaGFyc2V0PVVURi04Cm9yaWdpbjpodHRwOi8vbG9jYWxob3N0OjgwODAKc2VjLWZldGNoLXNpdGU6c2FtZS1vcmlnaW4Kc2VjLWZldGNoLW1vZGU6Y29ycwpzZWMtZmV0Y2gtZGVzdDplbXB0eQpyZWZlcmVyOmh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9jbWQvY21kLTAwNC5odG1sCmFjY2VwdC1lbmNvZGluZzpnemlwLCBkZWZsYXRlLCBicgphY2NlcHQtbGFuZ3VhZ2U6emgtQ04semgtVFc7cT0wLjksemg7cT0wLjgsZW4tVVM7cT0wLjcsZW47cT0wLjYKY29va2llOkhtX2x2dF9iZGZmMWMxZGNjZTk3MWMzZDk4NmY5YmUwOTIxYTBlZT0xNTk3MTI2NTkwLDE1OTcxOTYzMDAsMTU5NzI4NjUyMywxNTk4NDA2MTIxOyBJZGVhLWY1NTIyNDc0PTAzNzQyOWRiLWEyZTgtNDI2MC1hOGMzLWMxMTY5OTUyMTdiODsgX2pzcHhjbXM9NDFjZGRhYzk5YzBiNDkwMzg5OTMzNTgxN2NlMjVhN2M7IGNzcmZ0b2tlbj13amZiNVdIMVNwcks1dlc3WHVpWXdydGM2emhNbDBLSFJ0OTNreUFyNEw1aE9JWTBob01wMFM4cG9HZVVMUzFuOyBPRkJpei5WaXNpdG9yPTEwMDAxCg==";
        byte[] decodeData = Base64Decoder.decodeBase64FromString(encryptedData1);
        System.out.println(new String(decodeData));
    }

    @Test
    public void stringEncoder() {
        String name = "DongTai IAST";
        String charSet = "utf-8";
        String contentType = "application/json;charset=UTF-8";
        if (contentType.contains("charset")) {
            String[] contentTypes = contentType.split(";");
            for (String contentTypeItem : contentTypes) {
                if (contentTypeItem.contains("charset")) {
                    charSet = contentTypeItem.trim().replace("charset=", "");
                }
            }
        }
        try {
            String nameUtf8 = new String(name.getBytes(), charSet);
            System.out.println(nameUtf8);
        } catch (UnsupportedEncodingException e) {
            String nameUtf8 = new String(name.getBytes());
            System.out.println(nameUtf8);
        }
    }
}
