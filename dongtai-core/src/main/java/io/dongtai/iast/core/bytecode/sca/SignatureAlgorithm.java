package io.dongtai.iast.core.bytecode.sca;

import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 获取第三方组件的版本、SHA-1
 *
 * @author dongzhiyong@huoxian.cn
 */
public class SignatureAlgorithm {

    public static String getSignature(InputStream is, String algorithm) {
        String signature = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];

            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                digest.update(buffer, 0, len);
            }
            buffer = null;
            BigInteger bigInteger = new BigInteger(1, digest.digest());
            signature = String.format("%040x", bigInteger);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SCA_CALCULATE_JAR_SIGNATURE_FAILED"),
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return signature;
    }

    public static String getSignature(File file, String algorithm) {
        if (!file.exists()) {
            return null;
        }
        String signature = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            signature = getSignature(in, algorithm);
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("SCA_CALCULATE_JAR_SIGNATURE_FAILED"),
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
        }
        return signature;
    }

    public static String getSignature(String filename, String algorithm) {
        return SignatureAlgorithm.getSignature(new File(filename), algorithm);
    }
}
