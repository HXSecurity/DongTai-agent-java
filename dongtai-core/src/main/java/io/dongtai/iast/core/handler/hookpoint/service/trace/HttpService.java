package io.dongtai.iast.core.handler.hookpoint.service.trace;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.handler.bypass.BlackUrlBypass;
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
import java.util.HashMap;
import java.util.Map;

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
        } else if (HttpClient.matchApacheHttpComponents(this.matchedSignature)) {
            traceId = addTraceToApacheHttpComponents(event);
        }

        if (traceId != null && !traceId.isEmpty()) {
            event.traceId = traceId;
        }
    }

    public void addBypass(MethodEvent event) {
        HashMap<String, String> blackUrlHeaders = new HashMap<>();
        blackUrlHeaders.put(BlackUrlBypass.getHeaderKey(), String.valueOf(BlackUrlBypass.isBlackUrl()));
        if (HttpClient.matchJavaNetUrl(this.matchedSignature)) {
            addHeaderToJavaNetURL(event, blackUrlHeaders);
        } else if (HttpClient.matchApacheHttp4(this.matchedSignature)
                || HttpClient.matchApacheHttp5(this.matchedSignature)) {
            addHeaderToApacheHttpClient(event, blackUrlHeaders);
        } else if (HttpClient.matchApacheHttp3(this.matchedSignature)) {
            addHeaderToApacheHttpClientLegacy(event, blackUrlHeaders);
        } else if (HttpClient.matchOkhttp(this.matchedSignature)) {
            addHeaderToOkhttp(event, blackUrlHeaders);
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

    private void addHeaderToJavaNetURL(MethodEvent event, Map<String, String> headers) {
        if (event.objectInstance == null) {
            return;
        }
        try {
            if (event.objectInstance instanceof HttpURLConnection) {
                final HttpURLConnection connection = (HttpURLConnection) event.objectInstance;
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
        } catch (IllegalStateException ignore) {
        } catch (Throwable e) {
            DongTaiLog.debug("add header to okhttp client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
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

    private void addHeaderToApacheHttpClient(MethodEvent event, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        if (event.parameterInstances.length < 2) {
            return;
        }
        Object obj = event.parameterInstances[1];
        if (obj == null) {
            return;
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
                return;
            }
            for (Map.Entry<String, String> header : headers.entrySet()) {
                method.invoke(obj, header.getKey(), header.getValue());
            }
        } catch (Throwable e) {
            DongTaiLog.debug("add header to okhttp client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
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

    private void addHeaderToApacheHttpClientLegacy(MethodEvent event, Map<String, String> headers) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return;
        }
        try {
            Method method = ReflectUtils.getDeclaredMethodFromSuperClass(obj.getClass(),
                    "setRequestHeader", new Class[]{String.class, String.class});
            if (method == null) {
                return;
            }
            for (Map.Entry<String, String> header : headers.entrySet()) {
                method.invoke(obj, header.getKey(), header.getValue());
            }
        } catch (Throwable e) {
            DongTaiLog.debug("add header to okhttp client failed: {}, {}"
                    , e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
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

    private void addHeaderToOkhttp(MethodEvent event, Map<String, String> headers) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return;
        }
        try {
            String className = obj.getClass().getName();
            if (!HttpClient.matchAllOkhttpCallClass(className)) {
                return;
            }

            Field reqField = obj.getClass().getDeclaredField("originalRequest");
            boolean accessible = reqField.isAccessible();
            reqField.setAccessible(true);
            Object req = reqField.get(obj);

            Method methodNewBuilder = req.getClass().getMethod("newBuilder");
            Object reqBuilder = methodNewBuilder.invoke(req);
            Method methodAddHeader = reqBuilder.getClass().getMethod("addHeader", String.class, String.class);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                methodAddHeader.invoke(reqBuilder, header.getKey(), header.getValue());
            }
            Method methodBuild = reqBuilder.getClass().getMethod("build");
            Object newReq = methodBuild.invoke(reqBuilder);
            reqField.set(obj, newReq);
            reqField.setAccessible(accessible);
        } catch (Throwable e) {
            DongTaiLog.debug("add header to okhttp client failed: {}, {}",
                    e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
    }

    /**
     * 添加traceId到Apache HttpComponents的请求上
     *
     * @param event
     * @return
     */
    private String addTraceToApacheHttpComponents(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return null;
        }
        try {
            String className = obj.getClass().getName();
            if (!HttpClient.matchApacheHttpComponents(className)) {
                return null;
            }

            // 关于库的版本兼容性：
            // 在GA org.apache.httpcomponents:fluent-hc的[4.4, 4.5.14]这个区间的版本里的request字段是这个org.apache.http.client.fluent.InternalHttpRequest类型
            // private final InternalHttpRequest request;
            // 然后org.apache.http.client.fluent.InternalHttpRequest这个类继承的org.apache.http.message.AbstractHttpMessage上有个setHeader方法：
            //    @Override // org.apache.http.HttpMessage
            //    public void setHeader(String name, String value) {
            //        Args.notNull(name, "Header name");
            //        this.headergroup.updateHeader(new BasicHeader(name, value));
            //    }
            // 另外一提，org.apache.http.message.AbstractHttpMessage是在httpcomponents-httpcore:httpcore下的，它自从4.0-alpha5版本被添加了之后就没有变更过
            //
            // 在GA org.apache.httpcomponents:fluent-hc的[4.2, 4.4) 版本区间的request字段是org.apache.http.client.methods.HttpRequestBase类型，这个类属于依赖中的org.apache.httpcomponents:httpclient
            // private final HttpRequestBase request;
            // 在org.apache.httpcomponents:httpclient的[4.0.1, 4.2.6]版本区间，org.apache.http.client.methods.HttpRequestBase这个类是继承的org.apache.http.message.AbstractHttpMessage，此条分支可以与上面的合并
            // org.apache.httpcomponents:httpclient的[4.3, 4.5.14]区间内是继承的org.apache.http.client.methods.AbstractExecutionAwareRequest
            // org.apache.http.client.methods.AbstractExecutionAwareRequest自从4.3.4版本被添加依赖，一直继承的org.apache.http.message.AbstractHttpMessage，此条分支又可以与上面合并
            // 所有版本的实现最终都会直接继承或者间接继承到org.apache.http.message.AbstractHttpMessage，所以下面的操作才可以统一

            Field reqField = obj.getClass().getDeclaredField("request");
            reqField.setAccessible(true);
            Object internalHttpRequest = reqField.get(obj);
            Method setHeaderMethod = internalHttpRequest.getClass().getMethod("setHeader", String.class, String.class);

            // 然后把追踪的头加上
            final String traceId = ContextManager.nextTraceId();
            setHeaderMethod.invoke(internalHttpRequest, ContextManager.getHeaderKey(), traceId);
            setHeaderMethod.invoke(internalHttpRequest, ContextManager.getParentKey(), String.valueOf(EngineManager.getAgentId()));
            return traceId;
        } catch (Throwable e) {
            DongTaiLog.debug("add traceId header to apache http components failed: {}, {}", e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
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
        } else if (HttpClient.matchApacheHttpComponents(event.signature)) {
            return validateApacheHttpComponents(event);
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

    /**
     * 验证是否是合法的
     *
     * @param event
     * @return
     */
    public static boolean validateApacheHttpComponents(MethodEvent event) {
        Object obj = event.objectInstance;
        if (obj == null) {
            return false;
        }
        try {
            String className = obj.getClass().getName();
            if (!HttpClient.matchApacheHttpComponents(className)) {
                return false;
            }

            // 关于类的版本兼容性，详见 #addTraceToApacheHttpComponents方法
            Field reqField = obj.getClass().getDeclaredField("request");
            reqField.setAccessible(true);
            Object internalHttpRequest = reqField.get(obj);
            Object header = internalHttpRequest.getClass().getMethod("getFirstHeader", String.class).invoke(internalHttpRequest, ContextManager.getHeaderKey());
            // traceId header not exists
            if (header == null) {
                return true;
            }
        } catch (Throwable e) {
            DongTaiLog.debug("validate apache http components failed: {}, {}", e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return false;
    }

}
