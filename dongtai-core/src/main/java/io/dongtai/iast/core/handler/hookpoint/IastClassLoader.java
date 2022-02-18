package io.dongtai.iast.core.handler.hookpoint;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassLoader extends URLClassLoader {

    public IastClassLoader(ClassLoader classLoader, URL[] adapterJar) {
        super(adapterJar, classLoader);
    }

    @Override
    public Class<?> loadClass(String s) {
        try {
            return super.loadClass(s);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
