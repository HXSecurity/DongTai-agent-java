package io.dongtai.iast.core.handler.hookpoint.models.policy;

public interface ClassModel {
    String getInternalClassName();
    String getSuperClassName();
    String[] getInterfaces();
}
