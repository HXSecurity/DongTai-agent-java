package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.HttpClientUtils;
import io.dongtai.iast.core.utils.matcher.ConfigMatcher;
import io.dongtai.log.DongTaiLog;
import java.io.File;
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

    private static Method cloneRequestMethod;
    private static Method cloneResponseMethod;
    private static Class<?> CLASS_OF_SERVLET_PROXY;
    private static IastClassLoader iastClassLoader;
    public static volatile File IAST_REQUEST_JAR_PACKAGE;


    private static void createClassLoader(Object req) {
        try {
            if (iastClassLoader != null) {
                return;
            }
            if (PropertyUtils.getInstance().isDebug()) {
                IAST_REQUEST_JAR_PACKAGE = new File(
                        System.getProperty("java.io.tmpdir") + File.separator + "dongtai-api.jar");
            } else {
                IAST_REQUEST_JAR_PACKAGE = new File(
                        System.getProperty("java.io.tmpdir") + File.separator + "dongtai-api.jar");
                HttpClientUtils.downloadRemoteJar(
                        "/api/v1/engine/download?engineName=dongtai-api",
                        IAST_REQUEST_JAR_PACKAGE.getAbsolutePath()
                );
            }
            if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                iastClassLoader = new IastClassLoader(
                        req.getClass().getClassLoader(),
                        new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                );
                CLASS_OF_SERVLET_PROXY = iastClassLoader.loadClass("io.dongtai.api.ServletProxy");
                cloneRequestMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneRequest", Object.class, boolean.class);
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static void loadCloneRequestMethod() {
        if (cloneRequestMethod == null) {
            try {
                cloneRequestMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneRequest", Object.class, boolean.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadCloneResponseMethod() {
        if (cloneResponseMethod == null) {
            try {
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
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
            return cloneRequestMethod.invoke(null, req, isJakarta);
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
            loadCloneResponseMethod();
            return cloneResponseMethod.invoke(null, response, isJakarta);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static Map<String, Object> getRequestMeta(Object request)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getRequestMeta");
        return (Map<String, Object>) methodOfRequestMeta.invoke(request);
    }

    public static String getPostBody(Object request) {
        try {
            Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getPostBody");
            return (String) methodOfRequestMeta.invoke(request);
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        return null;
    }

    public static Map<String, Object> getResponseMeta(Object response) {
        Method methodOfRequestMeta = null;
        try {
            methodOfRequestMeta = response.getClass().getDeclaredMethod("getResponseMeta");
            return (Map<String, Object>) methodOfRequestMeta.invoke(response);
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
        return null;
    }

    /**
     * solve http request
     *
     * @param event method call event
     */
    public static void solveHttp(MethodEvent event)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (DongTaiLog.isDebugEnabled()) {
            DongTaiLog.debug(EngineManager.SCOPE_TRACKER.get().toString());
        }

        Map<String, Object> requestMeta = getRequestMeta(event.argumentArray[0]);
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.getInstance().disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }
        if (ConfigMatcher.getInstance().getBlackUrl(requestMeta)) {
            return;
        }

        // todo: add custom header escape
        EngineManager.enterHttpEntry(requestMeta);

        if (DongTaiLog.isDebugEnabled()) {
            DongTaiLog.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"),
                    event.signature);
        }
    }

}
