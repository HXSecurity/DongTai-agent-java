package io.dongtai.iast.core.handler.hookpoint.models.policy;

public class TaintPositionException extends Exception {
    public TaintPositionException(String message) {
        super(message);
    }

    public TaintPositionException(String message, Throwable cause) {
        super(message, cause);
    }
}
