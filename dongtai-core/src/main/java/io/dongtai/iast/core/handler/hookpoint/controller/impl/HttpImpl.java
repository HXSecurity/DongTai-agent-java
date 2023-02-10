package io.dongtai.iast.core.handler.hookpoint.controller.impl;

import io.dongtai.iast.common.config.*;
import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.common.scope.ScopeManager;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.hookpoint.IastClassLoader;
import io.dongtai.iast.core.utils.*;
import io.dongtai.iast.core.utils.matcher.ConfigMatcher;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

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
    private final static ThreadLocal<Map<String, Object>> REQUEST_META = new ThreadLocal<Map<String, Object>>();

    static {
        IAST_REQUEST_JAR_PACKAGE = new File(PropertyUtils.getTmpDir() + "dongtai-api.jar");
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
        } catch (Throwable e) {
            DongTaiLog.warn("HttpImpl createClassLoader failed", e);
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
                DongTaiLog.warn("load cloneResponse method failed", e);
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
            if (cloneRequestMethod == null) {
                return req;
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
            if (!ScopeManager.SCOPE_TRACKER.getScope(Scope.HTTP_ENTRY).in()) {
                return response;
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
            if (cloneResponseMethod == null) {
                return response;
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

    public static Map<String, Object> getRequestMeta(Object request) {
        try {
            Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getRequestMeta");
            return (Map<String, Object>) methodOfRequestMeta.invoke(request);
        } catch (Throwable e) {
            DongTaiLog.warn("HttpImpl getRequestMeta failed", e);
        }
        return new HashMap<String, Object>();
    }

    public static String getPostBody(Object request) {
        try {
            Method methodOfRequestMeta = request.getClass().getDeclaredMethod("getPostBody");
            return (String) methodOfRequestMeta.invoke(request);
        } catch (Throwable e) {
            DongTaiLog.warn("HttpImpl getPostBody failed", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getResponseMeta(Object response) {
        Method methodOfRequestMeta = null;
        try {
            methodOfRequestMeta = response.getClass().getDeclaredMethod("getResponseMeta", new Class[]{boolean.class});
            boolean getBody = ((Config<Boolean>) ConfigBuilder.getInstance().getConfig(ConfigKey.REPORT_RESPONSE_BODY)).get();
            return (Map<String, Object>) methodOfRequestMeta.invoke(response, getBody);
        } catch (Throwable e) {
            DongTaiLog.warn("HttpImpl getResponseMeta failed", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void solveHttpRequest(Object obj, Object req, Object resp, Map<String, Object> requestMeta) {
        if (requestMeta == null || requestMeta.size() == 0) {
            return;
        }
        REQUEST_META.set(requestMeta);

        try {
            Config<RequestDenyList> config = (Config<RequestDenyList>) ConfigBuilder.getInstance()
                    .getConfig(ConfigKey.REQUEST_DENY_LIST);
            RequestDenyList requestDenyList = config.get();
            if (requestDenyList != null) {
                String requestURL = ((StringBuffer) requestMeta.get("requestURL")).toString();
                Map<String, String> headers = (Map<String, String>) requestMeta.get("headers");
                if (requestDenyList.match(requestURL, headers)) {
                    DongTaiLog.trace("HTTP Request {} deny to collect", requestURL);
                    return;
                }
            }
        } catch (Throwable ignore) {
        }

        Boolean isReplay = (Boolean) requestMeta.get("replay-request");
        if (isReplay) {
            EngineManager.ENTER_REPLAY_ENTRYPOINT.enterEntry();
        }
        // todo Consider increasing the capture of html request responses
        if (ConfigMatcher.getInstance().disableExtension((String) requestMeta.get("requestURI"))) {
            return;
        }
        if (ConfigMatcher.getInstance().getBlackUrl(requestMeta)) {
            return;
        }

        EngineManager.enterHttpEntry(requestMeta);
        DongTaiLog.debug("HTTP Request:{} {} from: {}", requestMeta.get("method"), requestMeta.get("requestURI"),
                obj.getClass().getName());
    }

    public static Map<String, String> parseRequestHeaders(Object req, Enumeration<?> headerNames) {
        Map<String, String> headers = new HashMap<String, String>(32);
        Method getHeaderMethod = ReflectUtils.getDeclaredMethodFromSuperClass(req.getClass(),
                "getHeader", new Class[]{String.class});
        if (getHeaderMethod == null) {
            return headers;
        }
        while (headerNames.hasMoreElements()) {
            try {
                String key = (String) headerNames.nextElement();
                String val = (String) getHeaderMethod.invoke(req, key);
                headers.put(key, val);
            } catch (Throwable ignore) {
            }
        }
        return headers;
    }

    public static void solveHttpResponse(Object obj, Object req, Object resp, Collection<?> headerNames, int status) {
        Map<String, Collection<String>> headers = parseResponseHeaders(resp, headerNames);
        REQUEST_META.get().put("responseStatus", (String) REQUEST_META.get().get("protocol") + " " + status);
        REQUEST_META.get().put("responseHeaders", headers);
    }

    public static Map<String, Collection<String>> parseResponseHeaders(Object resp, Collection<?> headerNames) {
        Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>(32);
        Method getHeadersMethod = ReflectUtils.getDeclaredMethodFromSuperClass(resp.getClass(),
                "getHeaders", new Class[]{String.class});
        if (getHeadersMethod == null) {
            return headers;
        }
        for (Object key : headerNames) {
            try {
                Collection<String> val = (Collection<String>) getHeadersMethod.invoke(resp, key);
                headers.put((String) key, val);
            } catch (Throwable ignore) {
            }
        }
        return headers;
    }

    public static IastClassLoader getClassLoader() {
        return iastClassLoader;
    }
}
