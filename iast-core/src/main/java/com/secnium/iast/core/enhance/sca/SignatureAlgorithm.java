package com.secnium.iast.core.enhance.sca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 获取第三方组件的版本、SHA-1
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SignatureAlgorithm {

    public static String getSignture(InputStream is, String algorithm) {
        String signature = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024 * 1024 * 10];

            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            signature = new BigInteger(1, digest.digest()).toString(16);
        } catch (IOException e) {
            System.out.println(e);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return signature;
    }

    public static String getSignture(File file, String algorithm) {
        if (!file.exists()) {
            return null;
        }
        String signature = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            signature = getSignture(in, algorithm);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return signature;
    }

    public static String getSignture(String filename, String algorithm) {
        return SignatureAlgorithm.getSignture(new File(filename), algorithm);
    }
}
