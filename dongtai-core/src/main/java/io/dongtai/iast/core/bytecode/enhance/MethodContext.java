package io.dongtai.iast.core.bytecode.enhance;

import io.dongtai.iast.core.handler.hookpoint.models.policy.Signature;

public class MethodContext {
    private ClassContext declaredClass;
    private String signature;
    private String matchedSignature;
    private String methodName;
    private String[] parameters;
    private int modifier;
    private String descriptor;

    public MethodContext(ClassContext declaredClass, String methodName) {
        this.declaredClass = declaredClass;
        this.methodName = methodName;
    }

    public String getInternalClassName() {
        return this.declaredClass.getInternalClassName();
    }

    public String getClassName() {
        return this.declaredClass.getClassName();
    }

    public String getMatchedClassName() {
        return this.declaredClass.getMatchedClassName();
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String[] getParameters() {
        return this.parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public ClassContext getDeclaredClass() {
        return this.declaredClass;
    }

    public void setDeclaredClass(ClassContext declaredClass) {
        this.declaredClass = declaredClass;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public void updateSignature() {
        this.signature = Signature.normalizeSignature(this.getClassName(), this.methodName, this.parameters);
    }

    public void updateMatchedSignature() {
        this.matchedSignature = Signature.normalizeSignature(this.getMatchedClassName(), this.methodName, this.parameters);
    }

    public String getMatchedSignature() {
        if (this.matchedSignature == null) {
            updateMatchedSignature();
        }
        return this.matchedSignature;
    }

    @Override
    public String toString() {
        if (this.signature == null) {
            updateSignature();
        }
        return this.signature;
    }
}
