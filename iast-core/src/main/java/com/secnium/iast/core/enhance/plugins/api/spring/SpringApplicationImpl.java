package com.secnium.iast.core.enhance.plugins.api.spring;

import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.models.MethodEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.secnium.iast.core.report.ApiReport.sendReport;

/**
 * niuerzhuang@huoxian.cn
 */
public class SpringApplicationImpl {

    private static IastClassLoader iastClassLoader;
    public static Method getAPI;
    public static boolean isSend;

    public static void getWebApplicationContext(MethodEvent event) {
        if (!isSend) {
            Object applicationContext = event.returnValue;
            createClassLoader(applicationContext);
            loadApplicationContext();
            Map<String, Object> invoke = null;
            try {
                invoke = (Map<String, Object>) getAPI.invoke(null, applicationContext);
                sendReport(invoke);
                isSend = true;
            } catch (Exception ignored) {
            }
        }
    }

    private static void createClassLoader(Object applicationContext) {
        try {
            if (iastClassLoader == null) {
                if (HttpImpl.IAST_REQUEST_JAR_PACKAGE.exists()) {
                    Class<?> applicationContextClass = applicationContext.getClass();
                    URL[] adapterJar = new URL[]{HttpImpl.IAST_REQUEST_JAR_PACKAGE.toURI().toURL()};
                    iastClassLoader = new IastClassLoader(applicationContextClass.getClassLoader(), adapterJar);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void loadApplicationContext() {
        if (getAPI == null) {
            try {
                Class<?> proxyClass;
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.spring.SpringApplicationContext");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

}
