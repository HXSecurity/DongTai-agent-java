package io.dongtai.iast.core.handler.hookpoint.models.policy;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;

import java.util.Arrays;

public class SignatureMethodMatcher implements MethodMatcher {
    private final Signature signature;

    public SignatureMethodMatcher(Signature signature) {
        this.signature = signature;
    }

    public boolean match(MethodContext method) {
        if (!this.signature.getClassName().equals(method.getMatchedClassName())) {
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
