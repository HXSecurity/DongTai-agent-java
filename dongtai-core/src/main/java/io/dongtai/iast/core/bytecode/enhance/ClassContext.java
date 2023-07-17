package io.dongtai.iast.core.bytecode.enhance;

import org.objectweb.asm.ClassReader;

import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ClassContext {
    private String internalClassName;
    private String className;
    private String matchedClassName;
    private Set<String> ancestors;
    private Set<String> matchedClassSet;
    private String superClassName;
    private String[] interfaces;
    private int modifier;
    private boolean isBootstrapClassLoader;

    public ClassContext(ClassReader classReader, ClassLoader loader) {
        this.internalClassName = classReader.getClassName();
        this.className = this.internalClassName.replace("/", ".");
        this.superClassName = classReader.getSuperName();
        this.interfaces = classReader.getInterfaces();
        this.modifier = classReader.getAccess();
        this.isBootstrapClassLoader = (loader == null);
    }

    public ClassContext(String className, Set<String> ancestors, String[] interfaces,
                        int modifier, boolean isBootstrapClassLoader) {
        this.setClassName(className);
        this.setAncestors(ancestors);
        this.setInterface(interfaces);
        this.setModifier(modifier);
        this.setBootstrapClassLoader(isBootstrapClassLoader);
    }

    public String getInternalClassName() {
        return this.internalClassName;
    }

    public void setInternalClassName(String internalClassName) {
        this.internalClassName = internalClassName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Set<String> getAncestors() {
        return this.ancestors;
    }

    public void setAncestors(Set<String> ancestors) {
        this.ancestors = ancestors;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public String[] getInterfaces() {
        return this.interfaces;
    }

    public void setInterface(String[] interfaces) {
        this.interfaces = interfaces;
    }

    public int getModifier() {
        return this.modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    public String getMatchedClassName() {
        return this.matchedClassName;
    }

    public void setMatchedClassName(String matchedClassName) {
        this.matchedClassName = matchedClassName;
    }

    public boolean isBootstrapClassLoader() {
        return this.isBootstrapClassLoader;
    }

    public void setBootstrapClassLoader(boolean isBootstrapClassLoader) {
        this.isBootstrapClassLoader = isBootstrapClassLoader;
    }

    public Set<String> getMatchedClassSet() {
        return matchedClassSet;
    }

    public void setMatchedClassSet(Set<String> matchedClassSet) {
        this.matchedClassSet = matchedClassSet;
    }
}
