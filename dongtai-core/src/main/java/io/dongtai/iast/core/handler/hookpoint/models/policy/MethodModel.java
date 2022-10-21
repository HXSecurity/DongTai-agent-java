package io.dongtai.iast.core.handler.hookpoint.models.policy;

public interface MethodModel {
    String getInternalClassName();
    String getClassName();
    String getMethodName();
    String[] getParameters();
    ClassModel getDeclaredClass();
    int getModifier();
    String toString();
}
