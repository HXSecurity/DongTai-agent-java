package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import io.dongtai.iast.core.utils.*;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class SSRFSourceCheck implements SinkSourceChecker {
    public final static String SINK_TYPE = "ssrf";

    private static final String JAVA_NET_URL_CONN_GET_INPUT_STREAM = "sun.net.www.protocol.http.HttpURLConnection.getInputStream()";
    private static final String APACHE_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.http.client.methods.HttpRequestBase.setURI(java.net.URI)".substring(1);
    private static final String APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.commons.httpclient.HttpMethodBase.setURI(org.apache.commons.httpclient.URI)".substring(1);
    private static final String APACHE_HTTP_CLIENT5_EXECUTE = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)".substring(1);
    private static final String OKHTTP3_CALL_EXECUTE = "okhttp3.Call.execute()";
    private static final String OKHTTP3_CALL_ENQUEUE = "okhttp3.Call.enqueue(okhttp3.Callback)";
    private static final String OKHTTP_CALL_EXECUTE = "com.squareup.okhttp.Call.execute()";
    private static final String OKHTTP_CALL_ENQUEUE = "com.squareup.okhttp.Call.enqueue(com.squareup.okhttp.Callback)";

    private static final String APACHE_LEGACY_HTTP_CLIENT_URI = " org.apache.commons.httpclient.URI".substring(1);
    private static final String APACHE_HTTP_CLIENT5_REQUEST_INTERFACE = " org.apache.hc.core5.http.HttpRequest".substring(1);
    private static final String OKHTTP3_INTERNAL_REAL_CALL = "okhttp3.internal.connection.RealCall";
    private static final String OKHTTP3_REAL_CALL = "okhttp3.RealCall";
    private static final String OKHTTP_CALL = "com.squareup.okhttp.Call";

    private static final Set<String> SSRF_SINK_METHODS = new HashSet<String>(Arrays.asList(
            JAVA_NET_URL_CONN_GET_INPUT_STREAM,
            APACHE_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_HTTP_CLIENT5_EXECUTE,
            OKHTTP3_CALL_EXECUTE,
            OKHTTP3_CALL_ENQUEUE,
            OKHTTP_CALL_EXECUTE,
            OKHTTP_CALL_ENQUEUE
    ));

    private String policySignature;

    @Override
    public boolean match(MethodEvent event, SinkNode sinkNode) {
        if (sinkNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.policySignature = ((SignatureMethodMatcher) sinkNode.getMethodMatcher()).getSignature().toString();
        }

        return SINK_TYPE.equals(sinkNode.getVulType()) && SSRF_SINK_METHODS.contains(this.policySignature);
    }

    @Override
    public boolean checkSource(MethodEvent event, SinkNode sinkNode) {
        boolean hitTaintPool = false;
        if (JAVA_NET_URL_CONN_GET_INPUT_STREAM.equals(this.policySignature)) {
            return checkJavaNetURL(event, sinkNode);
        } else if (APACHE_HTTP_CLIENT_REQUEST_SET_URI.equals(this.policySignature)
                || APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI.equals(this.policySignature)) {
            return checkApacheHttpClient(event, sinkNode);
        } else if (APACHE_HTTP_CLIENT5_EXECUTE.equals(this.policySignature)) {
            return checkApacheHttpClient5(event, sinkNode);
        } else if (OKHTTP3_CALL_EXECUTE.equals(this.policySignature)
                || OKHTTP3_CALL_ENQUEUE.equals(this.policySignature)
                || OKHTTP_CALL_EXECUTE.equals(this.policySignature)
                || OKHTTP_CALL_ENQUEUE.equals(this.policySignature)) {
            return CheckOkhttp(event, sinkNode);
        }
        return hitTaintPool;
    }

    private boolean processJavaNetUrl(MethodEvent event, Object u) {
        try {
            if (!(u instanceof URL)) {
                return false;
            }

            final URL url = (URL) u;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", url.getProtocol());
                put("USERINFO", url.getUserInfo());
                put("HOST", url.getHost());
                put("PATH", url.getPath());
                put("QUERY", url.getQuery());
            }};

            event.setObjectValue(url, true);
            return addSourceType(event, sourceMap);
        } catch (Throwable e) {
            DongTaiLog.warn("java.net.URL get source failed: " + e.getMessage());
            return false;
        }
    }

    private boolean processJavaNetUri(MethodEvent event, Object u) {
        try {
            if (!(u instanceof URI)) {
                return false;
            }

            final URI uri = (URI) u;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", uri.getScheme());
                put("USERINFO", uri.getUserInfo());
                put("HOST", uri.getHost());
                put("PATH", uri.getPath());
                put("QUERY", uri.getQuery());
            }};

            event.setObjectValue(uri, true);
            return addSourceType(event, sourceMap);
        } catch (Throwable e) {
            DongTaiLog.warn("java.net.URI get source failed: " + e.getMessage());
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

            return processJavaNetUrl(event, u);
        } catch (IllegalAccessException ignore) {
        } catch (InvocationTargetException ignore) {
        }
        return false;
    }

    private boolean checkApacheHttpClient(MethodEvent event, SinkNode sinkNode) {
        try {
            if (event.parameterInstances.length < 1 || event.parameterInstances[0] == null) {
                return false;
            }

            final Object obj = event.parameterInstances[0];
            if (obj instanceof URI) {
                return processJavaNetUri(event, obj);
            } else if (APACHE_LEGACY_HTTP_CLIENT_URI.equals(obj.getClass().getName())) {
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
            DongTaiLog.warn("apache http client get source failed: " + e.getMessage());
            return false;
        }
    }

    private boolean checkApacheHttpClient5(MethodEvent event, SinkNode sinkNode) {
        try {
            if (event.parameterInstances.length < 2 || event.parameterInstances[1] == null) {
                return false;
            }

            final Object reqObj = event.parameterInstances[1];
            if (ReflectUtils.isImplementsInterface(reqObj.getClass(), APACHE_HTTP_CLIENT5_REQUEST_INTERFACE)) {
                final Object authorityObj = reqObj.getClass().getMethod("getAuthority").invoke(reqObj);
                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", reqObj.getClass().getMethod("getScheme").invoke(reqObj));
                    put("USERINFO", authorityObj.getClass().getMethod("getUserInfo").invoke(authorityObj));
                    put("HOST", authorityObj.getClass().getMethod("getHostName").invoke(authorityObj));
                    // getPath = path + query
                    put("PATHQUERY", reqObj.getClass().getMethod("getPath").invoke(reqObj));
                }};

                Object uriObj = reqObj.getClass().getMethod("getUri").invoke(reqObj);
                event.addParameterValue(1, uriObj, true);
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.warn("apache http client5 get source failed: " + e.getMessage());
            return false;
        }
    }

    private boolean CheckOkhttp(MethodEvent event, SinkNode sinkNode) {
        try {
            Class<?> cls = event.objectInstance.getClass();
            if (OKHTTP3_REAL_CALL.equals(cls.getName()) || OKHTTP3_INTERNAL_REAL_CALL.equals(cls.getName())
                    || OKHTTP_CALL.equals(cls.getName())) {
                Object url;

                if (OKHTTP_CALL.equals(cls.getName())) {
                    Field reqField = cls.getDeclaredField("originalRequest");
                    reqField.setAccessible(true);
                    Object req = reqField.get(event.objectInstance);
                    url = req.getClass().getMethod("httpUrl").invoke(req);
                } else {
                    Method reqMethod = cls.getDeclaredMethod("request");
                    reqMethod.setAccessible(true);
                    Object req = reqMethod.invoke(event.objectInstance);
                    url = req.getClass().getMethod("url").invoke(req);
                }

                if (url == null || !url.getClass().getName().endsWith("HttpUrl")) {
                    return false;
                }

                final Object fUrl = url;
                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", fUrl.getClass().getMethod("scheme").invoke(fUrl));
                    put("USERNAME", fUrl.getClass().getMethod("username").invoke(fUrl));
                    put("PASSWORD", fUrl.getClass().getMethod("password").invoke(fUrl));
                    put("HOST", fUrl.getClass().getMethod("host").invoke(fUrl));
                    put("PATH", fUrl.getClass().getMethod("encodedPath").invoke(fUrl));
                    put("QUERY", fUrl.getClass().getMethod("query").invoke(fUrl));
                }};

                event.setObjectValue(fUrl, true);
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Throwable e) {
            DongTaiLog.warn("okhttp get source failed: " + e.getMessage());
            return false;
        }
    }

    private boolean addSourceType(MethodEvent event, Map<String, Object> sourceMap) {
        boolean hit = false;
        event.sourceTypes = new ArrayList<MethodEvent.MethodEventSourceType>();
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            if (!"".equals(entry.getValue()) && TaintPoolUtils.poolContains(entry.getValue(), event)) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(entry.getValue()), entry.getKey()));
                hit = true;
            }
        }

        if (event.sourceTypes.size() == 0) {
            event.sourceTypes = null;
        }

        return hit;
    }
}
