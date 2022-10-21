package io.dongtai.iast.core.handler.hookpoint.models.policy;

public class VisitedMethod implements MethodModel {
    private String signature;
    private String internalClassName;
    private String className;
    private String methodName;
    private String[] parameters;
    private ClassModel declaredClass;
    private int modifier;
    private String descriptor;

    public VisitedMethod(ClassModel cls, String methodName) {
        this.internalClassName = cls.getInternalClassName();
        this.className = cls.getInternalClassName().replace("/", ".");
        this.methodName = methodName;
    }

    @Override
    public String getInternalClassName() {
        return this.internalClassName;
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public String[] getParameters() {
        return this.parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public ClassModel getDeclaredClass() {
        return declaredClass;
    }

    public void setDeclaredClass(ClassModel declaredClass) {
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
        this.signature = Signature.normalizeSignature(this.className, this.methodName, this.parameters);
    }

    @Override
    public String toString() {
        if (this.signature == null) {
            updateSignature();
        }
        return this.signature;
    }
}
