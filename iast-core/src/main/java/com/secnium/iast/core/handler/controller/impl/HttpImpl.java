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

    private static void loadCloneRequestMethod(boolean isJakarta) {
        if (cloneRequestMethod == null) {
            try {
                Class<?> proxyClass;

                proxyClass = isJakarta ?
                        iastClassLoader.loadClass("cn.huoxian.iast.jakarta.RequestWrapper") :
                        iastClassLoader.loadClass("cn.huoxian.iast.servlet.RequestWrapper");
                cloneRequestMethod = proxyClass.getDeclaredMethod("cloneRequest", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadCloneResponseMethod(boolean isJakarta) {
        if (cloneResponseMethod == null) {
            try {
                Class<?> proxyClass;
                proxyClass = isJakarta ?
                        iastClassLoader.loadClass("cn.huoxian.iast.jakarta.ResponseWrapper") :
                        iastClassLoader.loadClass("cn.huoxian.iast.servlet.ResponseWrapper");
                cloneResponseMethod = proxyClass.getDeclaredMethod("cloneResponse", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param req       request object
     * @param isJakarta Is it a jakarta api request object
     * @return
     */
    public static Object cloneRequest(Object req, boolean isJakarta) {

        try {
            createClassLoader(req);
            loadCloneRequestMethod(isJakarta);
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
     * Clone the response object, get the response header and response body
     *
     * @param response original response object
     * @return dongtai response object
     */
    public static Object cloneResponse(Object response, boolean isJakarta) {
        try {
            loadCloneResponseMethod(isJakarta);
            return cloneResponseMethod.invoke(null, response);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Map<String, Object> getRequestMeta(Object request, boolean isJakarta) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastRequestMethod) {
            createClassLoader(request);
            Class<?> proxyClass = isJakarta ?
                    iastClassLoader.loadClass("cn.huoxian.iast.jakarta.HttpRequest") :
                    iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpRequest");
            iastRequestMethod = proxyClass.getDeclaredMethod("getRequest", Object.class);
        }
        return (Map<String, Object>) iastRequestMethod.invoke(null, request);
    }

    public static Map<String, Object> getResponseMeta(Object response, boolean isJakarta) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastResponseMethod) {
            Class<?> proxyClass = isJakarta ?
                    iastClassLoader.loadClass("cn.huoxian.iast.jakarta.HttpResponse") :
                    iastClassLoader.loadClass("cn.huoxian.iast.servlet.HttpResponse");
            iastResponseMethod = proxyClass.getDeclaredMethod("getResponse", Object.class);
        }
        return (Map<String, Object>) iastResponseMethod.invoke(null, response);
    }

    /**
     * solve http request
     *
     * @param event method call event
     */
    public static void solveHttp(MethodEvent event) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (logger.isDebugEnabled()) {
            logger.debug(EngineManager.SCOPE_TRACKER.get().toString());
        }

        Map<String, Object> requestMeta = getRequestMeta(event.argumentArray[0], true);
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }
        // todo: add custom header escape
        EngineManager.enterHttpEntry(requestMeta);
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"), event.signature);
        }
    }

    static {
        try {
            IAST_REQUEST_JAR_PACKAGE = File.createTempFile("dongtai-api", ".jar");
            HttpClientUtils.downloadRemoteJar(
                    "/api/v1/engine/download?engineName=dongtai-servlet&jakarta=1",
                    IAST_REQUEST_JAR_PACKAGE.getAbsolutePath()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogUtils.getLogger(HttpImpl.class);
}
