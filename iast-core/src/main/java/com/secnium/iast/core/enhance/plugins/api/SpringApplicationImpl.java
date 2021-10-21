package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.controller.impl.HttpImpl;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.HttpClientUtils;

import java.io.File;
import java.io.IOException;
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

    public static void getWebApplicationContext(MethodEvent event, AtomicInteger invokeIdSequencer) {
        if (!isSend) {
            Object applicationContext = event.returnValue;
            createClassLoader(applicationContext);
            loadApplicationContext();
            Map<String, Object> invoke = null;
            try {
                invoke = (Map<String, Object>) getAPI.invoke(null, applicationContext);
                sendReport(invoke);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            isSend = true;
        }
    }

    private static void createClassLoader(Object applicationContext) {
        try {
            if (iastClassLoader == null) {
                try {
                    HttpImpl.IAST_REQUEST_JAR_PACKAGE = File.createTempFile("dongtai-api-", ".jar");
                    HttpClientUtils.downloadRemoteJar(
                            "/api/v1/engine/download?engineName=dongtai-api&jakarta=0",
                            HttpImpl.IAST_REQUEST_JAR_PACKAGE.getAbsolutePath()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (HttpImpl.IAST_REQUEST_JAR_PACKAGE.exists()) {
                    iastClassLoader = new IastClassLoader(
                            applicationContext.getClass().getClassLoader(),
                            new URL[]{HttpImpl.IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                    );
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
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.SpringApplicationContext");
                getAPI = proxyClass.getDeclaredMethod("getAPI", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        iastClassLoader.loadClass("org.springframework.context.ApplicationContext");
    }

}
