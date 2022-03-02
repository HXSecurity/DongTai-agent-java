package io.dongtai.iast.core.enums;

/**
 * 请求类型枚举类
 *
 * @author liyuan40
 * @date 2022/3/1 14:55
 */
public enum RequestTypeEnum {
    /**
     * 请求类型
     */
    HTTP("HTTP", "HTTP 请求"),
    DUBBO("Dubbo", "Dubbo 请求"),
    RPC("RPC", "RPC 请求"),
    ;

    private final String type;

    private final String desc;

    RequestTypeEnum(String requestType, String desc) {
        this.type = requestType;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
