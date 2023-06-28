package io.dongtai.iast.api.openapi.domain;

import com.alibaba.fastjson2.annotation.JSONField;

/**
 * 参数值所在的位置
 *
 * @author CC11001100
 * @since v1.12.0
 */
public enum ParameterIn {

    // 参数值在query string中
    Query("query"),

    // 参数值在path中
    Path("path"),

    // 参数值在cookie中
    Cookie("cookie"),

    // 参数值在请求头上
    Header("header");

    private String value;

    ParameterIn() {
    }

    ParameterIn(String value) {
        this.value = value;
    }

    // 使用value的值作为JSON序列化的值
    // https://github.com/alibaba/fastjson/wiki/enum_custom_serialization
    @JSONField
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
