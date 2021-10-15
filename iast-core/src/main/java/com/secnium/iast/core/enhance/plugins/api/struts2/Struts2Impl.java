package com.secnium.iast.core.enhance.plugins.api.struts2;

import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.models.MethodEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static com.secnium.iast.core.report.ApiReport.sendReport;

public class Struts2Impl {

    private static IastClassLoader iastClassLoader;
    public static Method getAPI;
    public static boolean isSend;

    public static void getDispatcher(MethodEvent event) {
        if (!isSend) {
            Object disptcher = event.returnValue;
            createClassLoader(disptcher);
            loadDispatcher();
            Map<String, Object> invoke = null;
            try {
                invoke = (Map<String, Object>) getAPI.invoke(null, disptcher);
                sendReport(invoke);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e){
                e.printStackTrace();
            }
            isSend = true;
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

    private static void loadDispatcher() {
        if (getAPI == null) {
            try {
                Class<?> proxyClass;
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.api.Struts2Dispatcher");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        iastClassLoader.loadClass("org.apache.struts2.dispatcher.Dispatcher");
    }


}
