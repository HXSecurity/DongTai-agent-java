package io.dongtai.iast.core.handler.hookpoint.vulscan.dynamic;

import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.utils.TaintPoolUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class SSRFSourceCheck implements SinkSourceChecker {
    public final static String SINK_TYPE = "ssrf";

    private static final String JAVA_NET_URL_OPEN_CONNECTION = "java.net.URL.openConnection()";
    private static final String JAVA_NET_URL_OPEN_CONNECTION_PROXY = "java.net.URL.openConnection(java.net.Proxy)";
    private static final String JAVA_NET_URL_OPEN_STREAM = "java.net.URL.openStream()";
    // TODO: use method execute for sink
    private static final String APACHE_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.http.client.methods.HttpRequestBase.setURI(java.net.URI)".substring(1);
    private static final String APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.commons.httpclient.HttpMethodBase.setURI(org.apache.commons.httpclient.URI)".substring(1);
    private static final String APACHE_HTTP_CLIENT5_EXECUTE = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)".substring(1);
    private static final String OKHTTP3_CALL_EXECUTE = "okhttp3.Call.execute()";
    private static final String OKHTTP3_CALL_ENQUEUE = "okhttp3.Call.enqueue(okhttp3.Callback)";
    private static final String OKHTTP_CALL_EXECUTE = "com.squareup.okhttp.Call.execute()";
    private static final String OKHTTP_CALL_ENQUEUE = "com.squareup.okhttp.Call.enqueue(com.squareup.okhttp.Callback)";

    private static final String APACHE_LEGACY_HTTP_CLIENT_URI = " org.apache.commons.httpclient.URI".substring(1);
    private static final String APACHE_HTTP_CLIENT5_HTTP_REQUEST = " org.apache.hc.client5.http.classic.methods.HttpUriRequestBase".substring(1);
    private static final String OKHTTP3_INTERNAL_REAL_CALL = "okhttp3.internal.connection.RealCall";
    private static final String OKHTTP3_REAL_CALL = "okhttp3.RealCall";
    private static final String OKHTTP_CALL = "com.squareup.okhttp.Call";

    private static final Set<String> SSRF_SINK_METHODS = new HashSet<>(Arrays.asList(
            JAVA_NET_URL_OPEN_CONNECTION,
            JAVA_NET_URL_OPEN_CONNECTION_PROXY,
            JAVA_NET_URL_OPEN_STREAM,
            APACHE_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_HTTP_CLIENT5_EXECUTE,
            OKHTTP3_CALL_EXECUTE,
            OKHTTP3_CALL_ENQUEUE,
            OKHTTP_CALL_EXECUTE,
            OKHTTP_CALL_ENQUEUE
    ));

    public boolean match(IastSinkModel sink) {
        return SINK_TYPE.equals(sink.getType()) && SSRF_SINK_METHODS.contains(sink.getSignature());
    }

    public boolean checkSource(MethodEvent event, IastSinkModel sink) {
        boolean hitTaintPool = false;
        if (JAVA_NET_URL_OPEN_CONNECTION.equals(sink.getSignature())
                || JAVA_NET_URL_OPEN_CONNECTION_PROXY.equals(sink.getSignature())
                || JAVA_NET_URL_OPEN_STREAM.equals(sink.getSignature())) {
            return checkJavaNetURL(event, sink);
        } else if (APACHE_HTTP_CLIENT_REQUEST_SET_URI.equals(sink.getSignature())
                || APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI.equals(sink.getSignature())) {
            return checkApacheHttpClient(event, sink);
        } else if (APACHE_HTTP_CLIENT5_EXECUTE.equals(sink.getSignature())) {
            return checkApacheHttpClient5(event, sink);
        } else if (OKHTTP3_CALL_EXECUTE.equals(sink.getSignature())
                || OKHTTP3_CALL_ENQUEUE.equals(sink.getSignature())
                || OKHTTP_CALL_EXECUTE.equals(sink.getSignature())
                || OKHTTP_CALL_ENQUEUE.equals(sink.getSignature())) {
            return CheckOkhttp(event, sink);
        }
        return hitTaintPool;
    }

    private static boolean processJavaNetUrl(MethodEvent event, Object u) {
        try {
            if (!(u instanceof URL)) {
                return false;
            }

            URL url = (URL) u;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", url.getProtocol());
                put("USERINFO", url.getUserInfo());
                put("HOST", url.getHost());
                put("PATH", url.getPath());
                put("QUERY", url.getQuery());
            }};

            event.setInValue(url.toString());
            return addSourceType(event, sourceMap);
        } catch (Exception e) {
            DongTaiLog.warn("java.net.URL get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean processJavaNetUri(MethodEvent event, Object u) {
        try {
            if (!(u instanceof URI)) {
                return false;
            }

            URI uri = (URI) u;
            Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                put("PROTOCOL", uri.getScheme());
                put("USERINFO", uri.getUserInfo());
                put("HOST", uri.getHost());
                put("PATH", uri.getPath());
                put("QUERY", uri.getQuery());
            }};

            event.setInValue(uri.toString());
            return addSourceType(event, sourceMap);
        } catch (Exception e) {
            DongTaiLog.warn("java.net.URI get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean checkJavaNetURL(MethodEvent event, IastSinkModel sink) {
        return processJavaNetUrl(event, event.object);
    }

    private static boolean checkApacheHttpClient(MethodEvent event, IastSinkModel sink) {
        try {
            if (event.argumentArray.length < 1 || event.argumentArray[0] == null) {
                return false;
            }

            Object obj = event.argumentArray[0];
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

                event.setInValue((String) obj.getClass().getMethod("toString").invoke(obj));
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Exception e) {
            DongTaiLog.warn("apache http client get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean checkApacheHttpClient5(MethodEvent event, IastSinkModel sink) {
        try {
            if (event.argumentArray.length < 2 || event.argumentArray[1] == null) {
                return false;
            }

            Object reqObj = event.argumentArray[1];
            if (APACHE_HTTP_CLIENT5_HTTP_REQUEST.equals(reqObj.getClass().getName())
                    || APACHE_HTTP_CLIENT5_HTTP_REQUEST.equals(reqObj.getClass().getSuperclass().getName())) {
                Object authorityObj = reqObj.getClass().getMethod("getAuthority").invoke(reqObj);
                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", reqObj.getClass().getMethod("getScheme").invoke(reqObj));
                    put("USERINFO", authorityObj.getClass().getMethod("getUserInfo").invoke(authorityObj));
                    put("HOST", authorityObj.getClass().getMethod("getHostName").invoke(authorityObj));
                    // getPath = path + query
                    put("PATHQUERY", reqObj.getClass().getMethod("getPath").invoke(reqObj));
                }};

                Object uriObj = reqObj.getClass().getMethod("getUri").invoke(reqObj);
                event.setInValue((String) uriObj.getClass().getMethod("toString").invoke(uriObj));
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Exception e) {
            DongTaiLog.warn("apache http client5 get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean CheckOkhttp(MethodEvent event, IastSinkModel sink) {
        try {
            Class<?> cls = event.object.getClass();
            if (OKHTTP3_REAL_CALL.equals(cls.getName()) || OKHTTP3_INTERNAL_REAL_CALL.equals(cls.getName())
                    || OKHTTP_CALL.equals(cls.getName())) {
                Object url;

                if (OKHTTP_CALL.equals(cls.getName())) {
                    Field reqField = cls.getDeclaredField("originalRequest");
                    reqField.setAccessible(true);
                    Object req = reqField.get(event.object);
                    url = req.getClass().getMethod("httpUrl").invoke(req);
                } else {
                    Method reqMethod = cls.getDeclaredMethod("request");
                    reqMethod.setAccessible(true);
                    Object req = reqMethod.invoke(event.object);
                    url = req.getClass().getMethod("url").invoke(req);
                }

                if (url == null || !url.getClass().getName().endsWith("HttpUrl")) {
                    return false;
                }

                Map<String, Object> sourceMap = new HashMap<String, Object>() {{
                    put("PROTOCOL", url.getClass().getMethod("scheme").invoke(url));
                    put("USERNAME", url.getClass().getMethod("username").invoke(url));
                    put("PASSWORD", url.getClass().getMethod("password").invoke(url));
                    put("HOST", url.getClass().getMethod("host").invoke(url));
                    put("PATH", url.getClass().getMethod("encodedPath").invoke(url));
                    put("QUERY", url.getClass().getMethod("query").invoke(url));
                }};

                event.setInValue((String) url.getClass().getMethod("toString").invoke(url));
                return addSourceType(event, sourceMap);
            }

            return false;
        } catch (Exception e) {
            DongTaiLog.warn("okhttp get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean addSourceType(MethodEvent event, Map<String, Object> sourceMap) {
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
