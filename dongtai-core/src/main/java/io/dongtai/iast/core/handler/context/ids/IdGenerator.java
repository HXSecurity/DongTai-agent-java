package io.dongtai.iast.core.handler.context.ids;


import java.security.SecureRandom;
import java.util.UUID;

/**
 * @author owefsad
 */
public class IdGenerator {
    private static class Holder {
        static final SecureRandom numberGenerator = new SecureRandom();
    }

    public static String newGlobalId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String newSpanId() {
        byte[] bytes = new byte[8];
        SecureRandom random = Holder.numberGenerator;
        random.nextBytes(bytes);

        long msb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }

        return toDigits(msb >> 32, 8) + toDigits(msb >> 16, 4) + toDigits(msb, 4);
    }

    private static String toDigits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
}
