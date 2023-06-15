package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.handler.hookpoint.service.HttpClient;
import io.dongtai.iast.core.utils.*;
import io.dongtai.log.DongTaiLog;
import sun.net.www.MessageHeader;

import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class SSRFSourceCheck implements SinkSourceChecker {
    public final static String SINK_TYPE = "ssrf";
    private String policySignature;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return SINK_TYPE.equals(sinkNode.getVulType()) && HttpClient.match(this.policySignature);
    }

    @Override
    public boolean checkSource(MethodEvent event, SinkNode sinkNode) {
        boolean hitTaintPool = false;
        if (HttpClient.matchJavaNetUrl(this.policySignature)) {
            return checkJavaNetURL(event, sinkNode);
        } else if (HttpClient.matchApacheHttp3(this.policySignature)) {
            return checkApacheHttpClientLegacy(event, sinkNode);
        } else if (HttpClient.matchApacheHttp4(this.policySignature)) {
            return checkApacheHttpClient(event, sinkNode);
        } else if (HttpClient.matchApacheHttp5(this.policySignature)) {
            return checkApacheHttpClient5(event, sinkNode);
        } else if (HttpClient.matchOkhttp(this.policySignature)) {
            return CheckOkhttp(event, sinkNode);
        }
        return hitTaintPool;
    }

    private boolean processJavaNetUrl(MethodEvent event, Object conn, Object u) {
        try {
            if (!(u instanceof URL)) {
                return false;
            }

            List<String> headerList = new ArrayList<String>();
            try {
                Field userHeadersField = ReflectUtils.getDeclaredFieldFromSuperClassByName(conn.getClass(), "userHeaders");
                if (userHeadersField == null) {
                    return false;
                }
                userHeadersField.setAccessible(true);
                Object userHeaders = userHeadersField.get(conn);
                if (userHeaders instanceof MessageHeader) {
                    Map<String, List<String>> headers = ((MessageHeader) userHeaders).getHeaders();
                    for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                        if (header.getKey().equals(ContextManager.getHeaderKey())) {
                            continue;
                        }
                        headerList.add(header.getKey());
                        headerList.addAll(header.getValue());
                    }
                }
            } catch (Throwable ignore) {
            }

            final URL url = (URL) u;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", url.getProtocol());
                put("USERINFO", url.getUserInfo());
                put("HOST", url.getHost());
                put("PATH", url.getPath());
                put("QUERY", url.getQuery());
                put("HEADER", headerList);
            }};

            event.setObjectValue(url, true);
            return addSourceType(event, sourceMap);
        } catch (Throwable e) {
            DongTaiLog.debug("java.net.URL get source failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean checkJavaNetURL(MethodEvent event, SinkNode sinkNode) {
        Object conn = event.objectInstance;
        if (conn == null) {
            return false;
        }

        try {
            Method getURLMethod = ReflectUtils.getDeclaredMethodFromSuperClass(conn.getClass(), "getURL", ObjectShare.EMPTY_CLASS_ARRAY);
            if (getURLMethod == null) {
                return false;
            }

            Object u = getURLMethod.invoke(conn);

            return processJavaNetUrl(event, conn, u);
        } catch (IllegalAccessException ignore) {
        } catch (InvocationTargetException ignore) {
        }
        return false;
    }

    private boolean checkApacheHttpClientLegacy(MethodEvent event, SinkNode sinkNode) {
        try {
            if (event.parameterInstances.length < 1 || event.parameterInstances[0] == null) {
                return false;
            }

            final Object obj = event.parameterInstances[0];
            if (HttpClient.APACHE_LEGACY_HTTP_CLIENT_URI.equals(obj.getClass().getName())) {
                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", obj.getClass().getMethod("getRawScheme").invoke(obj));
                    put("USERINFO", obj.getClass().getMethod("getRawUserinfo").invoke(obj));
                    put("HOST", obj.getClass().getMethod("getRawHost").invoke(obj));
                    put("PATH", obj.getClass().getMethod("getRawPath").invoke(obj));
                    put("QUERY", obj.getClass().getMethod("getRawQuery").invoke(obj));
                }};

                event.addParameterValue(0, obj, true);
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.debug("apache http legacy client get source failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean checkApacheHttpClient(MethodEvent event, SinkNode sinkNode) {
        try {
            if (event.parameterInstances.length < 2 || event.parameterInstances[1] == null) {
                return false;
            }

            final Object reqObj = event.parameterInstances[1];
            if (!ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT_REQUEST_INTERFACE)) {
                return false;
            }

            List<String> headerList = new ArrayList<String>();
            if (ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT_REQUEST_HEADER_INTERFACE)) {
                try {
                    Object[] headersObj = (Object[]) reqObj.getClass().getMethod("getAllHeaders").invoke(reqObj);
                    for (Object h : headersObj) {
                        String headerName = (String) h.getClass().getMethod("getName").invoke(h);
                        if (headerName == null || headerName.equals(ContextManager.getHeaderKey())) {
                            continue;
                        }
                        String headerValue = (String) h.getClass().getMethod("getValue").invoke(h);

                        headerList.add(headerName);
                        headerList.add(headerValue);
                    }
                } catch (Throwable ignore) {
                }
            }

            Object bodyObj = null;
            if (ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT_REQUEST_BODY_INTERFACE)) {
                try {
                    bodyObj = reqObj.getClass().getMethod("getEntity").invoke(reqObj);
                } catch (Throwable ignore) {
                }
            }
            final Object body = bodyObj;

            Object uriObj = reqObj.getClass().getMethod("getURI").invoke(reqObj);
            if (!(uriObj instanceof URI)) {
                return false;
            }

            final URI uri = (URI) uriObj;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", uri.getScheme());
                put("USERINFO", uri.getUserInfo());
                put("HOST", uri.getHost());
                put("PATH", uri.getPath());
                put("QUERY", uri.getQuery());
                put("HEADER", headerList);
                put("BODY", body);
            }};

            event.setObjectValue(uri, true);
            return addSourceType(event, sourceMap);
        } catch (Throwable e) {
            DongTaiLog.debug("apache http client get source failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean checkApacheHttpClient5(MethodEvent event, SinkNode sinkNode) {
        try {
            if (event.parameterInstances.length < 2 || event.parameterInstances[1] == null) {
                return false;
            }

            final Object reqObj = event.parameterInstances[1];
            if (!ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT5_REQUEST_INTERFACE)) {
                return false;
            }

            List<String> headerList = new ArrayList<String>();
            if (ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT5_REQUEST_HEADER_INTERFACE)) {
                try {
                    Object[] headersObj = (Object[]) reqObj.getClass().getMethod("getHeaders").invoke(reqObj);
                    for (Object h : headersObj) {
                        String headerName = (String) h.getClass().getMethod("getName").invoke(h);
                        if (headerName == null || headerName.equals(ContextManager.getHeaderKey())) {
                            continue;
                        }
                        String headerValue = (String) h.getClass().getMethod("getValue").invoke(h);

                        headerList.add(headerName);
                        headerList.add(headerValue);
                    }
                } catch (Throwable ignore) {
                }
            }

            Object bodyObj = null;
            if (ReflectUtils.isImplementsInterface(reqObj.getClass(), HttpClient.APACHE_HTTP_CLIENT5_REQUEST_BODY_INTERFACE)) {
                try {
                    bodyObj = reqObj.getClass().getMethod("getEntity").invoke(reqObj);
                } catch (Throwable ignore) {
                }
            }
            final Object body = bodyObj;

            final Object authorityObj = reqObj.getClass().getMethod("getAuthority").invoke(reqObj);
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", reqObj.getClass().getMethod("getScheme").invoke(reqObj));
                put("USERINFO", authorityObj.getClass().getMethod("getUserInfo").invoke(authorityObj));
                put("HOST", authorityObj.getClass().getMethod("getHostName").invoke(authorityObj));
                // getPath = path + query
                put("PATHQUERY", reqObj.getClass().getMethod("getPath").invoke(reqObj));
                put("HEADER", headerList);
                put("BODY", body);
            }};

            Object uriObj = reqObj.getClass().getMethod("getUri").invoke(reqObj);
            event.addParameterValue(1, uriObj, true);
            return addSourceType(event, sourceMap);
        } catch (Throwable e) {
            DongTaiLog.debug("apache http client5 get source failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean CheckOkhttp(MethodEvent event, SinkNode sinkNode) {
        try {
            Class<?> cls = event.objectInstance.getClass();
            if (HttpClient.matchAllOkhttpCallClass(cls.getName())) {
                Object url;

                Field reqField = cls.getDeclaredField("originalRequest");
                reqField.setAccessible(true);
                Object req = reqField.get(event.objectInstance);
                if (HttpClient.matchLegacyOkhttpCallClass(cls.getName())) {
                    url = req.getClass().getMethod("httpUrl").invoke(req);
                } else {
                    url = req.getClass().getMethod("url").invoke(req);
                }

                if (url == null || !url.getClass().getName().endsWith("HttpUrl")) {
                    return false;
                }

                final Object fUrl = url;

                Object queryList = null;
                try {
                    Field queryListField = fUrl.getClass().getDeclaredField("queryNamesAndValues");
                    queryListField.setAccessible(true);
                    queryList = queryListField.get(fUrl);
                } catch (Throwable ignore) {
                }

                List<String> headerList = new ArrayList<String>();
                try {
                    Object headersObj = req.getClass().getMethod("headers").invoke(req);
                    Map<String, List<String>> headersMap = (Map<String, List<String>>) headersObj.getClass().getMethod("toMultimap").invoke(headersObj);
                    for (Map.Entry<String, List<String>> header : headersMap.entrySet()) {
                        if (header.getKey().equals(ContextManager.getHeaderKey())) {
                            continue;
                        }
                        headerList.add(header.getKey());
                        headerList.addAll(header.getValue());
                    }
                } catch (Throwable ignore) {
                }

                Object bodyObj = null;
                try {
                    bodyObj = req.getClass().getMethod("body").invoke(req);
                } catch (Throwable ignore) {
                }

                final Object query = queryList;
                final Object body = bodyObj;
                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", fUrl.getClass().getMethod("scheme").invoke(fUrl));
                    put("USERNAME", fUrl.getClass().getMethod("username").invoke(fUrl));
                    put("PASSWORD", fUrl.getClass().getMethod("password").invoke(fUrl));
                    put("HOST", fUrl.getClass().getMethod("host").invoke(fUrl));
                    put("PATH", fUrl.getClass().getMethod("encodedPath").invoke(fUrl));
                    put("QUERY", query);
                    put("HEADER", headerList);
                    put("BODY", body);
                }};

                event.setObjectValue(fUrl, true);
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.debug("okhttp get source failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
            return false;
        }
    }

    private boolean addSourceType(MethodEvent event, Map<String, Object> sourceMap) {
        boolean hit = false;
        event.sourceTypes = new ArrayList<MethodEvent.MethodEventSourceType>();
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (("QUERY".equals(entry.getKey()) || "HEADER".equals(entry.getKey()))
                    && entry.getValue() instanceof List) {
                for (Object q : (List) entry.getValue()) {
                    checkTaintPool(event, entry.getKey(), q);
                }
            } else {
                checkTaintPool(event, entry.getKey(), entry.getValue());
            }
        }

        if (event.sourceTypes.size() == 0) {
            event.sourceTypes = null;
        }

        return hit;
    }

    private boolean checkTaintPool(MethodEvent event, String key, Object value) {
        if (!"".equals(value) && TaintPoolUtils.poolContains(value, event)) {
            long hash = TaintPoolUtils.getStringHash(value);
            event.sourceTypes.add(new MethodEvent.MethodEventSourceType(hash, key));
            return true;
        }
        return false;
    }
}
