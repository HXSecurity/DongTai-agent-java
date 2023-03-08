package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.context.ContextManager;
import io.dongtai.iast.core.handler.hookpoint.models.MethodEvent;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SignatureMethodMatcher;
import io.dongtai.iast.core.handler.hookpoint.service.HttpClient;
import io.dongtai.iast.core.utils.ReflectUtils;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;

public class HttpService implements ServiceTrace {
    private String matchedSignature;

    @Override
    public boolean match(MethodEvent event, PolicyNode policyNode) {
        if (policyNode.getMethodMatcher() instanceof SignatureMethodMatcher) {
            this.matchedSignature = ((SignatureMethodMatcher) policyNode.getMethodMatcher()).getSignature().toString();
        }

        return HttpClient.match(this.matchedSignature);
    }

    @Override
    public void addTrace(MethodEvent event, PolicyNode policyNode) {
        String traceId = null;
        if (HttpClient.matchJavaNetUrl(this.matchedSignature)) {
            traceId = addTraceToJavaNetURL(event);
        } else if (HttpClient.matchApacheHttp4(this.matchedSignature)
                || HttpClient.matchApacheHttp5(this.matchedSignature)) {
            traceId = addTraceToApacheHttpClient(event);
        } else if (HttpClient.matchApacheHttp3(this.matchedSignature)) {
            traceId = addTraceToApacheHttpClientLegacy(event);
        } else if (HttpClient.matchOkhttp(this.matchedSignature)) {
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
                connection.setRequestProperty(ContextManager.getParentKey(),
                        String.valueOf(EngineManager.getAgentId()));
                return traceId;
            }
        } catch (IllegalStateException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("add traceId header to java.net.URLConnection failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return null;
    }

    private String addTraceToApacheHttpClient(MethodEvent event) {
        if (event.parameterInstances.length < 2) {
            return null;
        }
        Object obj = event.parameterInstances[1];
        if (obj == null) {
            return null;
        }
        try {
            Method method;
            if (HttpClient.matchApacheHttp5(this.matchedSignature)) {
                method = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(),
                        "addHeader", new Class[]{String.class, Object.class});
            } else {
                method = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(),
                        "addHeader", new Class[]{String.class, String.class});
            }
            if (method == null) {
                return null;
            }
            final String traceId = ContextManager.nextTraceId();
            method.invoke(obj, ContextManager.getHeaderKey(), traceId);
            method.invoke(obj, ContextManager.getParentKey(), String.valueOf(EngineManager.getAgentId()));
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.debug("add traceId header to apache http client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return null;
    }

    private String addTraceToApacheHttpClientLegacy(MethodEvent event) {
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
            method.invoke(obj, ContextManager.getParentKey(), String.valueOf(EngineManager.getAgentId()));
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.debug("add traceId header to apache legacy http client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return null;
    }

    private String addTraceToOkhttp(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return null;
        }
        try {
            String className = obj.getClass().getName();
            if (!HttpClient.matchAllOkhttpCallClass(className)) {
                return null;
            }

            Field reqField = obj.getClass().getDeclaredField("originalRequest");
            boolean accessible = reqField.isAccessible();
            reqField.setAccessible(true);
            Object req = reqField.get(obj);

            Method methodNewBuilder = req.getClass().getMethod("newBuilder");
            Object reqBuilder = methodNewBuilder.invoke(req);
            Method methodAddHeader = reqBuilder.getClass().getMethod("addHeader", String.class, String.class);
            final String traceId = ContextManager.nextTraceId();
            methodAddHeader.invoke(reqBuilder, ContextManager.getHeaderKey(), traceId);
            methodAddHeader.invoke(reqBuilder, ContextManager.getParentKey(),
                    String.valueOf(EngineManager.getAgentId()));
            Method methodBuild = reqBuilder.getClass().getMethod("build");
            Object newReq = methodBuild.invoke(reqBuilder);
            reqField.set(obj, newReq);
            reqField.setAccessible(accessible);
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.debug("add traceId header to okhttp client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return null;
    }

    public static boolean validate(MethodEvent event) {
        if (HttpClient.matchJavaNetUrl(event.signature)) {
            return validateURLConnection(event);
        } else if (HttpClient.matchApacheHttp4(event.signature) || HttpClient.matchApacheHttp5(event.signature)) {
            return validateApacheHttpClient(event);
        } else if (HttpClient.matchOkhttp(event.signature)) {
            return validateOkhttp(event);
        }
        return true;
    }

    public static boolean validateURLConnection(MethodEvent event) {
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
            DongTaiLog.debug("validate URLConnection failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return false;
    }

    public static boolean validateApacheHttpClient(MethodEvent event) {
        if (event.parameterInstances.length < 2) {
            return false;
        }
        Object obj = event.parameterInstances[1];
        if (obj == null) {
            return false;
        }
        try {
            boolean v5 = false;
            if (!ReflectUtils.isImplementsInterface(obj.getClass(), HttpClient.APACHE_HTTP_CLIENT_REQUEST_HEADER_INTERFACE)
                    && !ReflectUtils.isImplementsInterface(obj.getClass(), HttpClient.APACHE_HTTP_CLIENT5_REQUEST_HEADER_INTERFACE)) {
                return false;
            }

            Method containsHeaderMethod = obj.getClass().getMethod("containsHeader", String.class);
            containsHeaderMethod.setAccessible(true);
            boolean containsHeader = (boolean) containsHeaderMethod.invoke(obj, ContextManager.getHeaderKey());
            // traceId header not exists
            if (!containsHeader) {
                return true;
            }
        } catch (Throwable e) {
            DongTaiLog.debug("validate apache http client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return false;
    }

    public static boolean validateOkhttp(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return false;
        }
        try {
            String className = obj.getClass().getName();
            if (!HttpClient.matchAllOkhttpCallClass(className)) {
                return false;
            }

            Field reqField = obj.getClass().getDeclaredField("originalRequest");
            reqField.setAccessible(true);
            Object req = reqField.get(obj);
            Object header = req.getClass().getMethod("header", String.class).invoke(req, ContextManager.getHeaderKey());
            // traceId header not exists
            if (header == null) {
                return true;
            }
        } catch (Throwable e) {
            DongTaiLog.debug("validate okhttp failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return false;
    }
}
