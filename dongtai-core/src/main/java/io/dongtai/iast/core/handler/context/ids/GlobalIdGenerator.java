package io.dongtai.iast.core.handler.context.ids;


import java.util.UUID;

/**
 * @author owefsad
 */
public class GlobalIdGenerator {

    private static final String PROCESS_ID = UUID.randomUUID().toString().replaceAll("-", "");

    /**
     * Generate a new id, combined by four parts.
     * <p>
     * The first one represents application instance id.
     * <p>
     * The second one represents agent id.
     * <p>
     * The third one represents thread id.
     * <p>
     * The fourth one represents span id, default is 0.
     *
     * @return unique id to represent a trace or segment
     */
    public static String generate(Integer agentId) {
        return PROCESS_ID
                + "-"
                + UUID.randomUUID().toString().replaceAll("-", "")
                + "."
                + agentId
                + "."
                + Thread.currentThread().getId()
                + "."
                + 0;
    }
}
