package io.dongtai.iast.core.handler.hookpoint.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HttpClient {

    private static final String JAVA_NET_URL_CONN = "sun.net.www.protocol.http.HttpURLConnection.connect()";
    private static final String JAVA_NET_URL_CONN_GET_INPUT_STREAM = "sun.net.www.protocol.http.HttpURLConnection.getInputStream()";
    private static final String JAVA_NET_URL_CONN_GET_OUTPUT_STREAM = "sun.net.www.protocol.http.HttpURLConnection.getOutputStream()";

    // 草这都是哪个GA里的类啊鬼能知道啊...
    private static final String APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.commons.httpclient.HttpMethodBase.setURI(org.apache.commons.httpclient.URI)".substring(1);
    private static final String APACHE_HTTP_CLIENT_EXECUTE = " org.apache.http.impl.client.CloseableHttpClient.doExecute(org.apache.http.HttpHost,org.apache.http.HttpRequest,org.apache.http.protocol.HttpContext)".substring(1);

    // GA: org.apache.httpcomponents:fluent-hc
    private static final String APACHE_HTTP_HTTPCOMPONENTS_EXECUTE = " org.apache.http.client.fluent.Request.execute()".substring(1);

    private static final String APACHE_HTTP_CLIENT5_EXECUTE = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)".substring(1);
    private static final String OKHTTP_CALL_EXECUTE = "com.squareup.okhttp.Call.execute()";
    private static final String OKHTTP_CALL_ENQUEUE = "com.squareup.okhttp.Call.enqueue(com.squareup.okhttp.Callback)";
    private static final String OKHTTP3_CALL_EXECUTE = "okhttp3.Call.execute()";
    private static final String OKHTTP3_CALL_ENQUEUE = "okhttp3.Call.enqueue(okhttp3.Callback)";

    public static final String APACHE_LEGACY_HTTP_CLIENT_URI = " org.apache.commons.httpclient.URI".substring(1);
    public static final String APACHE_HTTP_CLIENT_REQUEST_INTERFACE = " org.apache.http.HttpRequest".substring(1);
    public static final String APACHE_HTTP_CLIENT5_REQUEST_INTERFACE = " org.apache.hc.core5.http.HttpRequest".substring(1);
    public static final String APACHE_HTTP_CLIENT_REQUEST_HEADER_INTERFACE = " org.apache.http.HttpMessage".substring(1);
    public static final String APACHE_HTTP_CLIENT5_REQUEST_HEADER_INTERFACE = " org.apache.hc.core5.http.MessageHeaders".substring(1);
    public static final String APACHE_HTTP_CLIENT_REQUEST_BODY_INTERFACE = " org.apache.http.HttpEntityEnclosingRequest".substring(1);
    public static final String APACHE_HTTP_CLIENT5_REQUEST_BODY_INTERFACE = " org.apache.hc.core5.http.HttpEntityContainer".substring(1);

    private static final String OKHTTP_CALL = "com.squareup.okhttp.Call";
    private static final String OKHTTP3_REAL_CALL = "okhttp3.RealCall";
    // okhttp v4.x
    private static final String OKHTTP3_INTERNAL_REAL_CALL = "okhttp3.internal.connection.RealCall";

    private static final Set<String> SIGNATURE = new HashSet<String>(Arrays.asList(
            JAVA_NET_URL_CONN,
            JAVA_NET_URL_CONN_GET_INPUT_STREAM,
            JAVA_NET_URL_CONN_GET_OUTPUT_STREAM,
            APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_HTTP_CLIENT_EXECUTE,
            APACHE_HTTP_HTTPCOMPONENTS_EXECUTE,
            APACHE_HTTP_CLIENT5_EXECUTE,
            OKHTTP_CALL_EXECUTE,
            OKHTTP_CALL_ENQUEUE,
            OKHTTP3_CALL_EXECUTE,
            OKHTTP3_CALL_ENQUEUE
    ));

    private static final Set<String> JAVA_NET_URL_SIGNATURE = new HashSet<String>(Arrays.asList(
            JAVA_NET_URL_CONN,
            JAVA_NET_URL_CONN_GET_INPUT_STREAM,
            JAVA_NET_URL_CONN_GET_OUTPUT_STREAM
    ));

    private static final Set<String> OKHTTP_SIGNATURE = new HashSet<String>(Arrays.asList(
            OKHTTP_CALL_EXECUTE,
            OKHTTP_CALL_ENQUEUE,
            OKHTTP3_CALL_EXECUTE,
            OKHTTP3_CALL_ENQUEUE
    ));

    private static final Set<String> OKHTTP_CALL_CLASS = new HashSet<String>(Arrays.asList(
            OKHTTP_CALL,
            OKHTTP3_REAL_CALL,
            OKHTTP3_INTERNAL_REAL_CALL
    ));

    public static boolean match(String signature) {
        return SIGNATURE.contains(signature);
    }

    public static boolean matchJavaNetUrl(String signature) {
        return JAVA_NET_URL_SIGNATURE.contains(signature);
    }

    public static boolean matchApacheHttp3(String signature) {
        return APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI.equals(signature);
    }

    public static boolean matchApacheHttp4(String signature) {
        return APACHE_HTTP_CLIENT_EXECUTE.equals(signature);
    }

    public static boolean matchApacheHttp5(String signature) {
        return APACHE_HTTP_CLIENT5_EXECUTE.equals(signature);
    }
    
    public static boolean matchApacheHttpComponents(String signature) {
        return APACHE_HTTP_HTTPCOMPONENTS_EXECUTE.equals(signature);
    }

    public static boolean matchOkhttp(String signature) {
        return OKHTTP_SIGNATURE.contains(signature);
    }

    public static boolean matchAllOkhttpCallClass(String className) {
        return OKHTTP_CALL_CLASS.contains(className);
    }

    public static boolean matchLegacyOkhttpCallClass(String className) {
        return OKHTTP_CALL.equals(className);
    }
}
