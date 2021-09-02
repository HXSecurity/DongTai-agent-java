package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.matcher.ConfigMatcher;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Http方法处理入口
 *
 * @author dongzhiyong@huoxian.cn
 */
public class HttpImpl {
    public static Method iastRequestMethod;
    public static Method iastResponseMethod;
    public static Method cloneRequestMethod;
    public static Method cloneResponseMethod;
    private static IastClassLoader iastClassLoader;
    public static File IAST_REQUEST_JAR_PACKAGE;

    private static void createClassLoader(Object req) {
        try {
            if (iastClassLoader == null) {
                if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                    Class<?> reqClass = req.getClass();
                    URL[] adapterJar = new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()};
                    iastClassLoader = new IastClassLoader(reqClass.getClassLoader(), adapterJar);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void loadCloneRequestMethod() {
        if (cloneRequestMethod == null) {
            try {
                Class<?> proxyClass;

                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.RequestWrapper");
                cloneRequestMethod = proxyClass.getDeclaredMethod("cloneRequest", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadCloneResponseMethod() {
        if (cloneResponseMethod == null) {
            try {
                Class<?> proxyClass;

                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.ResponseWrapper");
                cloneResponseMethod = proxyClass.getDeclaredMethod("cloneResponse", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * fixme 测试分析该点师傅需要处理 javax/
     *
     * @param req
     * @return
     */
    public static Object cloneRequest(Object req) {

        try {
            createClassLoader(req);
            loadCloneRequestMethod();
            return cloneRequestMethod.invoke(null, req);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return req;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return req;
        }
    }

    /**
     * 克隆response对象，获取响应头、响应体
     *
     * @param response
     * @return
     */
    public static Object cloneResponse(Object response) {
        try {
            loadCloneResponseMethod();
            return cloneResponseMethod.invoke(null, response);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Map<String, Object> getRequestMeta(Object request, boolean javax) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastRequestMethod) {
            createClassLoader(request);
            Class<?> proxyClass;

            if (javax) {
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpRequest");
            } else {
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpRequestJakarta");
            }
            iastRequestMethod = proxyClass.getDeclaredMethod("getRequest", Object.class);
        }
        return (Map<String, Object>) iastRequestMethod.invoke(null, request);
    }

    public static Map<String, Object> getResponseMeta(Object response, boolean javax) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastResponseMethod) {
            Class<?> proxyClass;

            if (javax) {
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpResponse");
            } else {
                proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpResponseJakarta");
            }
            iastResponseMethod = proxyClass.getDeclaredMethod("getResponse", Object.class);
        }
        return (Map<String, Object>) iastResponseMethod.invoke(null, response);
    }

    /**
     * 处理http请求
     *
     * @param event 待处理的方法调用事件
     */
    public static void solveHttp(MethodEvent event) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (logger.isDebugEnabled()) {
            logger.debug(EngineManager.SCOPE_TRACKER.get().toString());
        }

        Map<String, Object> requestMeta = getRequestMeta(event.argumentArray[0], true);
        if (ConfigMatcher.disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }

        EngineManager.enterHttpEntry(requestMeta);
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"), event.signature);
        }
    }

    static {
        try {
            IAST_REQUEST_JAR_PACKAGE = File.createTempFile("dongtai-servlet", ".jar");
            HttpClientUtils.downloadRemoteJar(
                    "/api/v1/engine/download?package_name=dongtai-servlet",
                    IAST_REQUEST_JAR_PACKAGE.getAbsolutePath()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogUtils.getLogger(HttpImpl.class);
}
