package com.secnium.iast.core.handler.controller.impl;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.handler.IastClassLoader;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.util.HttpClientUtils;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.matcher.ConfigMatcher;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.slf4j.Logger;

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

    private static void createClassLoader(Object req, boolean isJakarta) {
        try {
            if (iastClassLoader == null) {
                try {
                    IAST_REQUEST_JAR_PACKAGE = File.createTempFile("dongtai-api-", ".jar");
                    HttpClientUtils.downloadRemoteJar(
                            "/api/v1/engine/download?engineName=dongtai-api&jakarta=" + (isJakarta ? 1 : 0),
                            IAST_REQUEST_JAR_PACKAGE.getAbsolutePath()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                    iastClassLoader = new IastClassLoader(
                            req.getClass().getClassLoader(),
                            new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                    );
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void loadCloneRequestMethod() {
        if (cloneRequestMethod == null) {
            try {
                Class<?> proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.api.RequestWrapper");
                cloneRequestMethod = proxyClass.getDeclaredMethod("cloneRequest", Object.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadCloneResponseMethod() {
        if (cloneResponseMethod == null) {
            try {
                Class<?> proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.api.ResponseWrapper");
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
            createClassLoader(req, isJakarta);
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
     * Clone the response object, get the response header and response body
     *
     * @param response original response object
     * @return dongtai response object
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

    public static Map<String, Object> getRequestMeta(Object request)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastRequestMethod) {
            Class<?> proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.api.HttpRequest");
            iastRequestMethod = proxyClass.getDeclaredMethod("getRequest", Object.class);
        }
        return (Map<String, Object>) iastRequestMethod.invoke(null, request);
    }

    public static Map<String, Object> getResponseMeta(Object response)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (null == iastResponseMethod) {
            Class<?> proxyClass = iastClassLoader.loadClass("cn.huoxian.iast.api.HttpResponse");
            iastResponseMethod = proxyClass.getDeclaredMethod("getResponse", Object.class);
            Method getOpenApi = proxyClass.getDeclaredMethod("getResponseLength", Integer.class);
            getOpenApi.invoke(null, PropertyUtils.getInstance().getResponseLength());
        }
        return (Map<String, Object>) iastResponseMethod.invoke(null, response);
    }

    /**
     * solve http request
     *
     * @param event method call event
     */
    public static void solveHttp(MethodEvent event)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (logger.isDebugEnabled()) {
            logger.debug(EngineManager.SCOPE_TRACKER.get().toString());
        }

        Map<String, Object> requestMeta = getRequestMeta(event.argumentArray[0]);
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }
        // todo: add custom header escape
        EngineManager.enterHttpEntry(requestMeta);
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"),
                    event.signature);
        }
    }

    private static final Logger logger = LogUtils.getLogger(HttpImpl.class);
}
