package io.dongtai.iast.agent.util;


/**
 * @author owefsad
 */
public class LogUtils {
    public static void info(String msg) {
        System.out.println("[io.dongtai.agent] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[io.dongtai.agent] " + msg);
    }
}
