package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.LinkedList;
import java.util.List;

public class VisitedClass implements ClassModel {
    private String internalClassName;
    private String superClassName;
    List<String> ancestors = new LinkedList<String>();
    private String[] interfaces;

    @Override
    public String getInternalClassName() {
        return this.internalClassName;
    }

    public void setInternalClassName(String internalClassName) {
        this.internalClassName = internalClassName;
    }

    @Override
    public String getSuperClassName() {
        return this.superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public List<String> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<String> ancestors) {
        this.ancestors = ancestors;
    }

    @Override
    public String[] getInterfaces() {
        return this.interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }
}
