package io.dongtai.iast.core.bytecode.enhance;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastContext {

    private static volatile IastContext instance;
    private String className;
    private String matchClassName;
    private Set<String> ancestors;
    private String[] interfaces;
    private int flags;
    private boolean enableAllHook;
    private boolean isBootstrapClassLoader;


    /**
     * 单例
     *
     * @return IastContext单例
     */
    public static IastContext getInstance() {
        assert instance != null;
        return instance;
    }

    public static IastContext build(String className, Set<String> ancestors, String[] interfaces, int flags, boolean isBootstrapClassLoader) {
        instance = new IastContext(className, ancestors, interfaces, flags, isBootstrapClassLoader);
        return instance;
    }

    private IastContext(String className, Set<String> ancestors, String[] interfaces,
                        int flags, boolean isBootstrapClassLoader) {
        this.setClassName(className);
        this.setAncestor(ancestors);
        this.setInterface(interfaces);
        this.setFlags(flags);
        this.setBootstrapClassLoader(isBootstrapClassLoader);
        this.setEnableAllHook(false);
    }


    public void setClassName(String className) {
        this.className = className;
    }

    public void setAncestor(Set<String> ancestors) {
        this.ancestors = ancestors;
    }

    public void setInterface(String[] interfaces) {
        this.interfaces = interfaces;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setLoader(ClassLoader loader) {
    }

    public int getFlags() {
        return this.flags;
    }

    public String getClassName() {
        return className;
    }

    public Set<String> getAncestors() {
        return ancestors;
    }

    public String getMatchClassName() {
        return matchClassName;
    }

    public void setMatchClassName(String matchClassName) {
        this.matchClassName = matchClassName;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public void setBootstrapClassLoader(boolean isBootstrapClassLoader) {
        this.isBootstrapClassLoader = isBootstrapClassLoader;
    }

    public boolean isBootstrapClassLoader() {
        return isBootstrapClassLoader;
    }

    public boolean isEnableAllHook() {
        return enableAllHook;
    }

    public void setEnableAllHook(boolean enableAllHook) {
        this.enableAllHook = enableAllHook;
    }
}
