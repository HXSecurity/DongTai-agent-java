package com.secnium.iast.agent.util;


public class LogUtils {
    public static void info(String msg) {
        // todo 增加时间打印
        System.out.println("[cn.huoxian.dongtai.agent] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[cn.huoxian.dongtai.agent] " + msg);
    }
}
