package io.dongtai.iast.core.utils.config.entity;

import lombok.Data;

/**
 * 获取服务端配置时，http响应实体
 */
@Data
public class ServerResponseEntity<T> {
    /**
     * 状态码
     */
    private Integer status;

    /**
     * 信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

}
