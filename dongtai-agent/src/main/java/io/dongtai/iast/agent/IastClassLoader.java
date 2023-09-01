package io.dongtai.iast.agent;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarFile;


/**
 * 代码参考自开源项目jvm-sandbox
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassLoader extends URLClassLoader {

    private final String toString;

    public IastClassLoader(final String jarFilePath) throws MalformedURLException {
        super(new URL[]{new URL("file:" + jarFilePath)});
        this.toString = String.format("IastClassLoader[path=%s;]", jarFilePath);
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (null != url) {
            return url;
        }
        url = super.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);
        if (null != urls) {
            return urls;
        }
        urls = super.getResources(name);
        return urls;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        if (!name.startsWith("io.dongtai") && !name.startsWith("java.lang.iast")) {
            return super.loadClass(name, resolve);
        }

        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Throwable e) {
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public String toString() {
        return toString;
    }


    @SuppressWarnings("unused")
    public void closeIfPossible() {
        // JDK6版本的 URLClassLoader 未继承Closeable接口，无法自动关闭，需要手动释放
        if (this instanceof Closeable) {
            try {
                ((Closeable) this).close();
            } catch (Throwable cause) {
            }
            return;
        }

        // 对于JDK6的版本，URLClassLoader要关闭起来就显得有点麻烦，这里弄了一大段代码来稍微处理下
        // 而且还不能保证一定释放干净了，至少释放JAR文件句柄是没有什么问题了
        try {
            final Object sun_misc_URLClassPath = forceGetDeclaredFieldValue(URLClassLoader.class, "ucp", this);
            final Object java_util_Collection = forceGetDeclaredFieldValue(sun_misc_URLClassPath.getClass(), "loaders",
                    sun_misc_URLClassPath);

            for (final Object sun_misc_URLClassPath_JarLoader :
                    ((Collection) java_util_Collection).toArray()) {
                try {
                    final JarFile java_util_jar_JarFile = forceGetDeclaredFieldValue(
                            sun_misc_URLClassPath_JarLoader.getClass(), "jar", sun_misc_URLClassPath_JarLoader);
                    java_util_jar_JarFile.close();
                } catch (Throwable t) {
                    // if we got this far, this is probably not a JAR loader so skip it
                }
            }

        } catch (Throwable cause) {
            // ignore...
        }

    }

    private <T> T forceGetDeclaredFieldValue(Class<?> clazz, String name, Object target)
            throws NoSuchFieldException, IllegalAccessException {
        final Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(target);
    }

}
