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
    public static File IAST_REQUEST_JAR_PACKAGE;
    private final static ThreadLocal<Map<String, Object>> REQUEST_META = new ThreadLocal<>();

    static {
        IAST_REQUEST_JAR_PACKAGE = new File(System.getProperty("java.io.tmpdir.dongtai") + "iast" + File.separator + "dongtai-api.jar");
        if (!IAST_REQUEST_JAR_PACKAGE.exists()) {
            HttpClientUtils.downloadRemoteJar("/api/v1/engine/download?engineName=dongtai-api", IAST_REQUEST_JAR_PACKAGE.getAbsolutePath());
        }
    }


    private static void createClassLoader(Object req) {
        try {
            if (iastClassLoader != null) {
                return;
            }
            if (IAST_REQUEST_JAR_PACKAGE.exists()) {
                iastClassLoader = new IastClassLoader(
                        req.getClass().getClassLoader(),
                        new URL[]{IAST_REQUEST_JAR_PACKAGE.toURI().toURL()}
                );
                CLASS_OF_SERVLET_PROXY = iastClassLoader.loadClass("io.dongtai.api.ServletProxy");
                if (CLASS_OF_SERVLET_PROXY == null) {
                    return;
                }
                cloneRequestMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneRequest", Object.class, boolean.class);
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
            }
        } catch (MalformedURLException | NoSuchMethodException e) {
            DongTaiLog.error("io.dongtai.iast.core.handler.hookpoint.controller.impl.HttpImpl.createClassLoader(java.lang.Object)",e);
        }
    }

    private static void loadCloneResponseMethod() {
        if (cloneResponseMethod == null) {
            try {
                if (CLASS_OF_SERVLET_PROXY == null) {
                    return;
                }
                cloneResponseMethod = CLASS_OF_SERVLET_PROXY
                        .getDeclaredMethod("cloneResponse", Object.class, boolean.class);
            } catch (NoSuchMethodException e) {
                DongTaiLog.error(e);
            }
        }
    }

    /**
     * @param req       request object
     * @param isJakarta Is it a jakarta api request object
     * @return
     */
    public static Object cloneRequest(Object req, boolean isJakarta) {
        if (req == null) {
            return null;
        }
        try {
            if (cloneRequestMethod == null) {
                createClassLoader(req);
            }
            return cloneRequestMethod.invoke(null, req, isJakarta);
        } catch (IllegalAccessException e) {
            return req;
        } catch (InvocationTargetException e) {
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
            if (response == null) {
                return null;
            }
            if (ConfigMatcher.getInstance().disableExtension((String) REQUEST_META.get().get("requestURI"))) {
                return response;
            }
            if (ConfigMatcher.getInstance().getBlackUrl(REQUEST_META.get())) {
                return response;
            }
            if (cloneResponseMethod == null) {
                loadCloneResponseMethod();
            }
            return cloneResponseMethod.invoke(null, response, isJakarta);
        } catch (IllegalAccessException e) {
            return response;
        } catch (InvocationTargetException e) {
            return response;
        } finally {
            REQUEST_META.remove();
        }
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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            DongTaiLog.error(e);
        }
        return null;
    }

    public static Map<String, Object> getResponseMeta(Object response) {
        Method methodOfRequestMeta = null;
        try {
            methodOfRequestMeta = response.getClass().getDeclaredMethod("getResponseMeta");
            return (Map<String, Object>) methodOfRequestMeta.invoke(response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            DongTaiLog.error(e);
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
        DongTaiLog.debug(EngineManager.SCOPE_TRACKER.get().toString());
        REQUEST_META.set(getRequestMeta(event.argumentArray[0]));
        Boolean isReplay = (Boolean) REQUEST_META.get().get("replay-request");
        if (isReplay){
            EngineManager.ENTER_REPLAY_ENTRYPOINT.enterEntry();
        }
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.getInstance().disableExtension((String) REQUEST_META.get().get("requestURI"))) {
            return;
        }
        if (ConfigMatcher.getInstance().getBlackUrl(REQUEST_META.get())) {
            return;
        }

        // todo: add custom header escape
        EngineManager.enterHttpEntry(REQUEST_META.get());
        DongTaiLog.debug("HTTP Request:{} {} from: {}", REQUEST_META.get().get("method"), REQUEST_META.get().get("requestURI"),
                event.signature);
    }

    public static IastClassLoader getClassLoader() {
        return iastClassLoader;
    }
}
