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

public class SSRFSourceCheck {
    private static final String JAVA_NET_URL_OPEN_CONNECTION = "java.net.URL.openConnection()";
    private static final String JAVA_NET_URL_OPEN_CONNECTION_PROXY = "java.net.URL.openConnection(java.net.Proxy)";
    private static final String JAVA_NET_URL_OPEN_STREAM = "java.net.URL.openStream()";
    // TODO: use method execute for sink
    private static final String APACHE_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.http.client.methods.HttpRequestBase.setURI(java.net.URI)".substring(1);
    private static final String APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.commons.httpclient.HttpMethodBase.setURI(org.apache.commons.httpclient.URI)".substring(1);
    private static final String OKHTTP3_CALL_EXECUTE = "okhttp3.Call.execute()";
    private static final String OKHTTP3_CALL_ENQUEUE = "okhttp3.Call.enqueue(okhttp3.Callback)";
    private static final String OKHTTP_CALL_EXECUTE = "com.squareup.okhttp.Call.execute()";
    private static final String OKHTTP_CALL_ENQUEUE = "com.squareup.okhttp.Call.enqueue(com.squareup.okhttp.Callback)";

    private static final String APACHE_LEGACY_HTTP_CLIENT_METHOD_BASE = " org.apache.commons.httpclient.HttpMethodBase".substring(1);
    private static final String OKHTTP3_INTERNAL_REAL_CALL = "okhttp3.internal.connection.RealCall";
    private static final String OKHTTP3_REAL_CALL = "okhttp3.RealCall";
    private static final String OKHTTP_CALL = "com.squareup.okhttp.Call";

    private static final Set<String> SSRF_SINK_METHODS = new HashSet<>(Arrays.asList(
            JAVA_NET_URL_OPEN_CONNECTION,
            JAVA_NET_URL_OPEN_CONNECTION_PROXY,
            JAVA_NET_URL_OPEN_STREAM,
            APACHE_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI,
            OKHTTP3_CALL_EXECUTE,
            OKHTTP3_CALL_ENQUEUE,
            OKHTTP_CALL_EXECUTE,
            OKHTTP_CALL_ENQUEUE
    ));

    public static boolean isSinkMethod(IastSinkModel sink) {
        return SSRF_SINK_METHODS.contains(sink.getSignature());
    }

    public static boolean sourceHitTaintPool(MethodEvent event, IastSinkModel sink) {
        boolean hitTaintPool = false;
        if (JAVA_NET_URL_OPEN_CONNECTION.equals(sink.getSignature())
                || JAVA_NET_URL_OPEN_CONNECTION_PROXY.equals(sink.getSignature())
                || JAVA_NET_URL_OPEN_STREAM.equals(sink.getSignature())) {
            return javaNetURLSourceHit(event, sink);
        } else if (APACHE_HTTP_CLIENT_REQUEST_SET_URI.equals(sink.getSignature())
                || APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI.equals(sink.getSignature())) {
            return apacheHttpClientRequestSetURISourceHit(event, sink);
        } else if (OKHTTP3_CALL_EXECUTE.equals(sink.getSignature())
                || OKHTTP3_CALL_ENQUEUE.equals(sink.getSignature())
                || OKHTTP_CALL_EXECUTE.equals(sink.getSignature())
                || OKHTTP_CALL_ENQUEUE.equals(sink.getSignature())) {
            return okhttpSourceHit(event, sink);
        }
        return hitTaintPool;
    }

    private static boolean processJavaNetUrl(MethodEvent event, Object u) {
        try {
            if (!(u instanceof URL)) {
                return false;
            }

            URL url = (URL) u;
            String protocol = url.getProtocol();
            String userInfo = url.getUserInfo();
            String host = url.getHost();
            String path = url.getPath();
            String query = url.getQuery();

            event.setInValue(url.toString());
            return addSourceType(event, protocol, userInfo, host, path, query);
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
            String protocol = uri.getScheme();
            String userInfo = uri.getUserInfo();
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();

            event.setInValue(uri.toString());
            return addSourceType(event, protocol, userInfo, host, path, query);
        } catch (Exception e) {
            DongTaiLog.warn("java.net.URI get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean javaNetURLSourceHit(MethodEvent event, IastSinkModel sink) {
        return processJavaNetUrl(event, event.object);
    }

    private static boolean apacheHttpClientRequestSetURISourceHit(MethodEvent event, IastSinkModel sink) {
        try {
            if (event.argumentArray.length < 1 || event.argumentArray[0] == null) {
                return false;
            }

            Object obj = event.argumentArray[0];
            if (event.argumentArray[0] instanceof URI) {
                return processJavaNetUri(event, obj);
            } else if (APACHE_LEGACY_HTTP_CLIENT_METHOD_BASE.equals(obj.getClass().getName())) {
                String protocol = (String) obj.getClass().getMethod("getScheme").invoke(obj);
                String userInfo = (String) obj.getClass().getMethod("getUserinfo").invoke(obj);
                String host = (String) obj.getClass().getMethod("getHost").invoke(obj);
                String path = (String) obj.getClass().getMethod("getPath").invoke(obj);
                String query = (String) obj.getClass().getMethod("getQuery").invoke(obj);

                event.setInValue((String) obj.getClass().getMethod("toString").invoke(obj));
                return addSourceType(event, protocol, userInfo, host, path, query);
            }

            return false;
        } catch (Exception e) {
            DongTaiLog.warn("apache http client get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean okhttpSourceHit(MethodEvent event, IastSinkModel sink) {
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

                String protocol = (String) url.getClass().getMethod("scheme").invoke(url);
                String userInfo = (String) url.getClass().getMethod("username").invoke(url);
                String host = (String) url.getClass().getMethod("host").invoke(url);
                String path = (String) url.getClass().getMethod("encodedPath").invoke(url);
                String query = (String) url.getClass().getMethod("query").invoke(url);

                event.setInValue((String) url.getClass().getMethod("toString").invoke(url));
                return addSourceType(event, protocol, userInfo, host, path, query);
            }

            return false;
        } catch (Exception e) {
            DongTaiLog.warn("okhttp get source failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean addSourceType(MethodEvent event, String protocol, String userInfo, String host, String path, String query) {
        boolean hit1 = !"".equals(protocol) && TaintPoolUtils.poolContains(protocol, event);
        boolean hit2 = !"".equals(userInfo) && TaintPoolUtils.poolContains(userInfo, event);
        boolean hit3 = !"".equals(host) && TaintPoolUtils.poolContains(host, event);
        boolean hit4 = !"".equals(path) && TaintPoolUtils.poolContains(path, event);
        boolean hit5 = !"".equals(query) && TaintPoolUtils.poolContains(query, event);
        event.inValue = event.object.toString();
        boolean hit = hit1 || hit2 || hit3 || hit4 || hit5;
        if (hit && event.sourceTypes == null) {
            event.sourceTypes = new ArrayList<MethodEvent.MethodEventSourceType>();
            if (hit1) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(protocol), "PROTOCOL"));
            }
            if (hit2) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(userInfo), "USERINFO"));
            }
            if (hit3) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(host), "HOST"));
            }
            if (hit4) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(path), "PATH"));
            }
            if (hit5) {
                event.sourceTypes.add(new MethodEvent.MethodEventSourceType(System.identityHashCode(query), "QUERY"));
            }
        }
        return hit;
    }
}
