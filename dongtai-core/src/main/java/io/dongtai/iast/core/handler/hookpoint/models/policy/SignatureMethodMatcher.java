package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.Arrays;

public class SignatureMethodMatcher implements MethodMatcher {
    private final Signature signature;

    public SignatureMethodMatcher(Signature signature) {
        this.signature = signature;
    }

    public boolean match(MethodModel method) {
        if (!this.signature.getClassName().equals(method.getClassName())) {
            return false;
        }
        if (!this.signature.getMethodName().equals(method.getMethodName())) {
            return false;
        }

        return Arrays.equals(this.signature.getParameters(), method.getParameters());
    }

    public Signature getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return getClass().getName() + "/" + this.signature.toString();
    }
}
