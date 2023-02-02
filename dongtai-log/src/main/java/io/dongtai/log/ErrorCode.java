package io.dongtai.log;

public enum ErrorCode {
    AGENT_PREMAIN_INVOKE_FAILED(10101, "agent premain invoke failed");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
