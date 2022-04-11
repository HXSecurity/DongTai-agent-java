package io.dongtai.iast.common.enums.code;

/**
 * 接口公共返回码
 *
 * @author chenyi
 * @date 2022/3/17
 */
public enum CommonResultCode {
    /**
     * 默认返回码
     */
    DEFAULT(0, ""),
    /**
     * 请求成功返回码
     */
    SUCCESS(201, "success"),
    ;

    public final int status;
    public final String msg;

    CommonResultCode(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
