package io.dongtai.iast.core.utils;

import java.util.UUID;

/**
 * @author owefsad
 */
public class TraceIdGenerator {

    private static final String PROCESS_ID = UUID.randomUUID().toString().replaceAll("-", "");

    public static String generate(String agentId) {
        return PROCESS_ID
                + ".0."
                + Thread.currentThread().getId()
                + "."
                + agentId;
    }
}
