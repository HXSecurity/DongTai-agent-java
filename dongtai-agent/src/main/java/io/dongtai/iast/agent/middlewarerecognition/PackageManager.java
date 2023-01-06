package io.dongtai.iast.agent.middlewarerecognition;


/**
 * @author dongzhiyong@huoxian.cn
 */
public final class PackageManager {
    private final String classname;

    public PackageManager(String classname) {
        this.classname = classname;
    }

    public Package getPackage() {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(this.classname);
            return clazz.getPackage();
        } catch (Throwable e) {
            return null;
        }
    }
}

