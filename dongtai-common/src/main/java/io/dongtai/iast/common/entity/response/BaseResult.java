package io.dongtai.iast.common.entity.response;

import io.dongtai.iast.common.enums.code.CommonResultCode;

import java.io.Serializable;

/**
 * 基础返回结果
 *
 * @author chenyi
 * @date 2022/3/17
 */
public class BaseResult implements Serializable {
    private static final long serialVersionUID = -5129661673717087206L;

    /**
     * 标识本次调用是否成功
     */
    private Boolean success;

    /**
     * 状态码
     */
    private int status;

    /**
     * 本次调用返回的消息
     */
    private String msg;


    public BaseResult() {
        this.status = CommonResultCode.DEFAULT.status;
        this.msg = CommonResultCode.DEFAULT.msg;
    }

    /**
     * 设置错误信息
     *
     * @param code    调用返回code
     * @param message 调用返回的消息
     */
    public <R extends BaseResult> R setErrorMessage(int code, String message) {
        this.status = code;
        this.success = false;
        this.msg = message;
        return (R) this;
    }

    /**
     * 设置错误信息
     *
     * @param rc   公共返回码
     * @param args 参数
     * @return 返回结果
     * @see CommonResultCode
     */
    public <R extends BaseResult> R setError(CommonResultCode rc, Object... args) {
        this.status = rc.status;
        this.success = false;
        if (args == null || args.length == 0) {
            this.msg = rc.msg;
        } else {
            this.msg = String.format(rc.msg, args);
        }
        return (R) this;
    }


    public boolean isSuccess() {
        if (success != null) {
            return success;
        }
        return CommonResultCode.SUCCESS.status == getStatus();
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
