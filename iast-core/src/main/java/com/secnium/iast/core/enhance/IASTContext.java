package com.secnium.iast.core.enhance;

import com.secnium.iast.core.handler.models.MethodEvent;

import java.security.CodeSource;
import java.util.HashSet;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IASTContext {
    private static volatile IASTContext instance;
    private String className;
    private String matchClassname;
    private HashSet<String> ancestors;
    private String baseClassName;
    private String[] interfaces;
    private int flags;
    private byte[] srcCodeByte;
    private CodeSource codeSource;
    private ClassLoader classLoader;
    private int listenId;
    private String namespace;
    private int classLoaderObjectID;
    private Map<Integer, MethodEvent> trackMap;
    private int version;
    private boolean enableAllHook;

    /**
     * 单例
     *
     * @return IASTContext单例
     */
    public static IASTContext getInstance() {
        assert instance != null;
        return instance;
    }

    public static IASTContext build(String className, String matchClassname, HashSet<String> ancestors, String[] interfaces,
                                    String baseClassName, int flags, byte[] srcCodeBytes, CodeSource codeSource,
                                    ClassLoader loader, int listenId, String namespace, int classLoaderObjectID) {
        instance = new IASTContext(className, matchClassname, ancestors, interfaces, baseClassName, flags, srcCodeBytes,
                codeSource, loader, listenId, namespace, classLoaderObjectID);
        return instance;
    }

    private IASTContext(String className, String matchClassname, HashSet<String> ancestors, String[] interfaces,
                        String baseClassName, int flags, byte[] srcCodeBytes, CodeSource codeSource, ClassLoader loader,
                        int listenId, String namespace, int classLoaderObjectID) {
        this.setClassName(className);
        this.setMatchClassname(matchClassname);
        this.setAncestor(ancestors);
        this.setInterface(interfaces);
        this.setBaseClassName(baseClassName);
        this.setFlags(flags);
        this.setBytecode(srcCodeBytes);
        this.setCodeSource(codeSource);
        this.setLoader(loader);
        this.setListenId(listenId);
        this.setNamespace(namespace);
        this.setClassLoaderObjectID(classLoaderObjectID);
        this.setEnableAllHook(false);
    }


    public void setClassName(String className) {
        this.className = className;
    }

    public void setAncestor(HashSet<String> ancestors) {
        this.ancestors = ancestors;
    }

    public void setInterface(String[] interfaces) {
        this.interfaces = interfaces;
    }

    public void setBaseClassName(String classname) {
        this.baseClassName = classname;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public void setBytecode(byte[] codebyte) {
        this.srcCodeByte = codebyte;
    }

    public void setCodeSource(CodeSource codeSource) {
        this.codeSource = codeSource;
    }

    public void setLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    public int getFlags() {
        return this.flags;
    }

    public String getClassName() {
        return className;
    }

    public HashSet<String> getAncestors() {
        return ancestors;
    }

    public void setListenId(int listenId) {
        this.listenId = listenId;
    }

    public int getListenId() {
        return listenId;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setClassLoaderObjectID(int classLoaderObjectID) {
        this.classLoaderObjectID = classLoaderObjectID;
    }

    public int getClassLoaderObjectID() {
        return classLoaderObjectID;
    }

    public String getMatchClassname() {
        return matchClassname;
    }

    public void setMatchClassname(String matchClassname) {
        this.matchClassname = matchClassname;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public String getBaseClassName() {
        return baseClassName;
    }

    public CodeSource getCodeSource() {
        return codeSource;
    }

    public byte[] getSrcCodeByte() {
        return srcCodeByte;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Map<Integer, MethodEvent> getTrackMap() {
        return trackMap;
    }

    public void setTrackMap(Map<Integer, MethodEvent> trackMap) {
        this.trackMap = trackMap;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isEnableAllHook() {
        return enableAllHook;
    }

    public void setEnableAllHook(boolean enableAllHook) {
        this.enableAllHook = enableAllHook;
    }
}
