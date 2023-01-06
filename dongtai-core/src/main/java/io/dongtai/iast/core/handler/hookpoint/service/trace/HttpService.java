package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.*;

public class HttpService implements ServiceTrace {
    private static final String JAVA_NET_URL_CONN_GET_INPUT_STREAM = "sun.net.www.protocol.http.HttpURLConnection.getInputStream()";
    private static final String APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.commons.httpclient.HttpMethodBase.setURI(org.apache.commons.httpclient.URI)".substring(1);
    private static final String APACHE_HTTP_CLIENT_REQUEST_SET_URI = " org.apache.http.client.methods.HttpRequestBase.setURI(java.net.URI)".substring(1);
    private static final String APACHE_HTTP_CLIENT5_EXECUTE = " org.apache.hc.client5.http.impl.classic.CloseableHttpClient.doExecute(org.apache.hc.core5.http.HttpHost,org.apache.hc.core5.http.ClassicHttpRequest,org.apache.hc.core5.http.protocol.HttpContext)".substring(1);
    private static final String OKHTTP_CALL_EXECUTE = "com.squareup.okhttp.Call.execute()";

    private static final Set<String> SIGNATURE = new HashSet<String>(Arrays.asList(
            JAVA_NET_URL_CONN_GET_INPUT_STREAM,
            APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_HTTP_CLIENT_REQUEST_SET_URI,
            APACHE_HTTP_CLIENT5_EXECUTE,
            OKHTTP_CALL_EXECUTE
    ));

    private String matchedSignature;

    @Override
    public boolean match(MethodEvent event, PolicyNode policyNode) {
        if (policyNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.matchedSignature = ((SignatureMethodMatcher) policyNode.getMethodMatcher()).getSignature().toString();
        }

        return SIGNATURE.contains(this.matchedSignature);
    }

    @Override
    public void addTrace(MethodEvent event, PolicyNode policyNode) {
        String traceId = null;
        if (JAVA_NET_URL_CONN_GET_INPUT_STREAM.equals(this.matchedSignature)) {
            traceId = addTraceToJavaNetURL(event);
        } else if (APACHE_HTTP_CLIENT5_EXECUTE.equals(this.matchedSignature)
                || APACHE_HTTP_CLIENT_REQUEST_SET_URI.equals(this.matchedSignature)) {
            traceId = addTraceToApacheHttpClient(event);
        } else if (APACHE_LEGACY_HTTP_CLIENT_REQUEST_SET_URI.equals(this.matchedSignature)) {
            traceId = addTraceToApacheLegacyHttpClient(event);
        } else if (OKHTTP_CALL_EXECUTE.equals(this.matchedSignature)) {
            traceId = addTraceToOkhttp(event);
        }

        if (traceId != null && !traceId.isEmpty()) {
            event.traceId = traceId;
        }
    }

    private String addTraceToJavaNetURL(MethodEvent event) {
        if (event.objectInstance == null) {
            return null;
        }
        try {
            if (event.objectInstance instanceof HttpURLConnection) {
                final HttpURLConnection connection = (HttpURLConnection) event.objectInstance;
                final String traceId = ContextManager.nextTraceId();
                connection.setRequestProperty(ContextManager.getHeaderKey(), traceId);
                return traceId;
            } else if (event.objectInstance instanceof HttpsURLConnection) {
                final HttpsURLConnection connection = (HttpsURLConnection) event.objectInstance;
                final String traceId = ContextManager.nextTraceId();
                connection.setRequestProperty(ContextManager.getHeaderKey(), traceId);
                return traceId;
            }
        } catch (IllegalStateException ignore) {
        } catch (Throwable e) {
            DongTaiLog.warn("add traceId header to java.net.URLConnection failed", e);
        }
        return null;
    }

    private String addTraceToApacheHttpClient(MethodEvent event) {
        Object obj;
        if (APACHE_HTTP_CLIENT5_EXECUTE.equals(this.matchedSignature)) {
            obj = event.parameterInstances[1];
        } else {
            obj = event.objectInstance;
        }
        if (obj == null) {
            return null;
        }
        try {
            Method method = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(),
                    "addHeader", new Class[]{String.class, String.class});
            if (method == null) {
                return null;
            }
            final String traceId = ContextManager.nextTraceId();
            method.invoke(obj, ContextManager.getHeaderKey(), traceId);
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.warn("add traceId header to apache http client failed", e);
        }
        return null;
    }

    private String addTraceToApacheLegacyHttpClient(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return null;
        }
        try {
            Method method = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(),
                    "setRequestHeader", new Class[]{String.class, String.class});
            if (method == null) {
                return null;
            }
            final String traceId = ContextManager.nextTraceId();
            method.invoke(obj, ContextManager.getHeaderKey(), traceId);
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.warn("add traceId header to apache legacy http client failed", e);
        }
        return null;
    }

    private String addTraceToOkhttp(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return null;
        }
        try {
            Field reqField = obj.getClass().getDeclaredField("originalRequest");
            boolean accessible = reqField.isAccessible();
            reqField.setAccessible(true);
            Object req = reqField.get(event.objectInstance);
            Method methodNewBuilder = req.getClass().getMethod("newBuilder");
            Object reqBuilder = methodNewBuilder.invoke(req);
            Method methodAddHeader = req.getClass().getMethod("addHeader", String.class, String.class);
            final String traceId = ContextManager.nextTraceId();
            methodAddHeader.invoke(reqBuilder, ContextManager.getHeaderKey(), traceId);
            Method methodBuild = req.getClass().getMethod("build");
            Object newReq = methodBuild.invoke(reqBuilder);
            reqField.set(obj, newReq);
            reqField.setAccessible(accessible);
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.warn("add traceId header to okhttp client failed", e);
        }
        return null;
    }

    public static boolean validateURLConnection(MethodEvent event) {
        if (!JAVA_NET_URL_CONN_GET_INPUT_STREAM.equals(event.signature)) {
            return true;
        }

        Object obj = event.objectInstance;
        if (obj == null) {
            return false;
        }

        try {
            // check if the traceId header has been set (by spring cloud etc...)
            Field userHeadersField = ReflectUtils.getDeclaredFieldFromSuperClassByName(obj.getClass(), "userHeaders");
            if (userHeadersField == null) {
                return false;
            }
            userHeadersField.setAccessible(true);
            Object userHeaders = userHeadersField.get(obj);
            Method getKeyMethod = userHeaders.getClass().getMethod("getKey", String.class);
            int hasKey = (int) getKeyMethod.invoke(userHeaders, ContextManager.getHeaderKey());
            // already has traceId header
            if (hasKey != -1) {
                return false;
            }

            Field inputStreamField = ReflectUtils.getDeclaredFieldFromSuperClassByName(obj.getClass(), "inputStream");
            if (inputStreamField == null) {
                return false;
            }
            inputStreamField.setAccessible(true);
            Object inputStream = inputStreamField.get(obj);

            // inputStream has cache, only first invoke getInputStream() need to collect
            if (inputStream == null) {
                return true;
            }
        } catch (Throwable e) {
            DongTaiLog.warn("validate URLConnection failed", e);
        }
        return false;
    }
}
