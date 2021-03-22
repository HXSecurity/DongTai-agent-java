package com.secnium.iast.core.util;

/**
 * 创建HTTP请求方法的枚举对象
 *
 * @author dongzhiyong@huoxian.cn
 */
public enum HttpMethods {
    /**
     * GET方法
     */
    GET("GET"),
    /**
     * POST方法
     */
    POST("POST");

    private final String method;

    HttpMethods(String method) {
        this.method = method;
    }

    public boolean equals(String method) {
        return this.method.equals(method.toUpperCase());
    }
}
