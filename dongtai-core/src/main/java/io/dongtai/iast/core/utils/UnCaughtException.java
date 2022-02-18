package io.dongtai.iast.core.utils;

/**
 * 未捕获异常
 * 用来封装不希望抛出的异常
 *
 * @author luanjia@taobao.com
 * @date 16/5/21
 * @modify dongzhiyong@huoxian.cn
 */
public class UnCaughtException extends RuntimeException {

    public UnCaughtException(Throwable cause) {
        super(cause);
    }
}
