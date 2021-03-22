package com.secnium.iast.core.util;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class RSAUtils {

    private static final String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5hIGsrKhzobovomu0qdgHWbrrVZr8pOwuFUoDYWqaw+GlPn/nR+abMO3nvlRqI7XmQwMCl2vAKwT5tu9QyVxqadgxfIssFCkruZFubrnqSYXmsrgu4h/26VBLBzRo0PvQNa3TUDetwHqy5My4YTfye55978AQqStjX0c3Q1S2ewIDAQAB";
    /**
     * 最大加密字节数，超出最大字节数需要分组加密
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA公钥加密
     *
     * @param str 加密字符串
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str) throws Exception {
        final byte[] decoded = DatatypeConverter.parseBase64Binary(PUBLIC_KEY);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] inputArray = str.getBytes("utf-8");
        int inputLength = inputArray.length;
        int offSet = 0;
        byte[] resultBytes = {};
        byte[] cache = {};
        while (inputLength - offSet > 0) {
            if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                offSet += MAX_ENCRYPT_BLOCK;
            } else {
                cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                offSet = inputLength;
            }
            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
            System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
        }
        return DatatypeConverter.printBase64Binary(resultBytes);
    }
}
