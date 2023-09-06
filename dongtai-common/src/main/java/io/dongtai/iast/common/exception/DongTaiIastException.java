package io.dongtai.iast.common.exception;

/**
 * 动态Agent整个项目内异常的基类，以后的异常尽量都继承这个类
 *
 * @author CC11001100
 * @since 1.13.2
 */
public class DongTaiIastException extends Exception {

    public DongTaiIastException() {
    }

    public DongTaiIastException(String message) {
        super(message);
    }

    public DongTaiIastException(String message, Throwable cause) {
        super(message, cause);
    }

    public DongTaiIastException(Throwable cause) {
        super(cause);
    }

    public DongTaiIastException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
