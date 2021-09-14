package com.secnium.iast.agent;

public class LogUtils {
    public static void info(String msg) {
        System.out.println("[cn.huoxian.dongtai.agent] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[cn.huoxian.dongtai.agent] " + msg);
    }
}
