package com.secnium.iast.core.util.http;

import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.Asserts;
import com.secnium.iast.core.util.ThrowableUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class HttpRequest {
    private WeakReference<Object> requestReference;
    private String method;
    private String protocol;
    private String scheme;
    private String version;
    private String uri;
    private String url;
    private String queryString;
    private String remoteIp;
    private String remoteHost;
    private String remoteAddr;
    private int remotePort;
    private String serverName;
    private int serverPort = -1;
    private String localName;
    private String localAddr;
    private int localPort;
    private boolean secure;

    private int port;
    private Map<String, String[]> parameters;
    private Enumeration<String> headerNames;
    private String headerValues;
    private Map<String, String[]> headers;
    private Object[] cookies;
    private String cookieValues;
    private String contextPath;
    private boolean parsedParameters;
    private boolean parsedMultipartParameters;
    private boolean analyzing;
    private int scope;
    private byte[] cachedBody;
    private String cachedBodyStr;
    private HashMap<String, Object> properties;
    private String cachedContentType;
    private Integer cachedContentLength;
    private boolean cachedXForwardedFor;
    private String[] cachedXForwardedForHeaders;
    private boolean cachedXForwardedForAsString;
    private String cachedXForwardedForHeadersAsString;
    private boolean checkedForDeserializer;
    private long startTime;
    private long elapsed;

    public void setClassOfHttpRequest(Class classOfHttpRequest) {
        this.classOfHttpRequest = classOfHttpRequest;
    }

    private Class classOfHttpRequest;

    public HttpRequest(Object httpServletRequest) {
        this.headerValues = null;
        this.setRequestReference(new WeakReference<Object>(httpServletRequest));
        lazyInitRequestFiled();
    }

    /**
     * 针对HttpRequest中需要用到的字段进行初始化，避免初始化暂不需要的字段，导致性能损耗
     */
    private void lazyInitRequestFiled() {
        Object requestObj = getRequestReference().get();
        if (null != requestObj) {
            // fixme: 可延后初始化的字段
            this.setClassOfHttpRequest(requestObj.getClass());
            this.setSecure((Boolean) invokeHandler("isSecure"));
            this.setMethod((String) invokeHandler("getMethod"));
            this.setUri((String) invokeHandler("getRequestURI"));
            this.setUrl((StringBuffer) invokeHandler("getRequestURL"));
        }
    }

    /**
     * 获取web目录,Weblogic 默认以war包部署的时候不能用getRealPath,xxx.getResource("/")获取
     * 的是当前应用所在的类路径，截取到WEB-INF之后的路径就是当前应用的web根目录了
     * <p>
     * fixme: 调用getSession时，触发set-cookie，导致无法准确的获取用户cookie的生命周期
     *
     * @param request
     * @return
     */
    public String getDocumentRootPath(Object request) {
        try {
            Object session = this.invokeHandler(classOfHttpRequest, requestReference.get(), "getSession", new Class[]{boolean.class}, new Object[]{false});
            Asserts.NOT_NULL("request.session is null", session);
            Object servletContext = invokeHandler(session.getClass(), session, "getServletContext");
            Class servletContextClass = servletContext.getClass();
            Method methodOfGetRealPath = servletContextClass.getMethod("getRealPath", String.class);
            String webRoot = (String) methodOfGetRealPath.invoke(servletContext, "/");

            int majorVersion = (Integer) servletContextClass.getMethod("getMajorVersion").invoke(servletContext);

            if (webRoot == null) {
                try {
                    // 检测Servlet版本,Servlet3.0之前ServletContext没有getClassLoader方法
                    if (majorVersion > 2) {
                        ClassLoader classLoader = servletContextClass.getClassLoader();

                        if (classLoader != null && StringUtils.isNotEmpty(classLoader.getResource("/").toURI().toURL().toString())) {
                            webRoot = classLoader.getResource("/").getPath();
                        }
                    } else if (StringUtils.isNotEmpty((String) servletContextClass.getMethod("getResource", String.class).invoke(servletContext, "/"))) {
                        webRoot = (String) servletContextClass.getMethod("getResource", String.class).invoke(servletContext, "/");
                    }
                } catch (Exception e) {
                    ;
                }

                ClassLoader requestClassLoader = request.getClass().getClassLoader();

                if (webRoot == null && requestClassLoader != null) {
                    // getResource("/")可能会获取不到Resourcezx
                    if (StringUtils.isNotEmpty(String.valueOf(requestClassLoader.getResource("/")))) {
                        webRoot = requestClassLoader.getResource("/").getPath();
                    } else if (StringUtils.isNotEmpty(String.valueOf(requestClassLoader.getResource("")))) {
                        webRoot = requestClassLoader.getResource("").getPath();
                    }
                }

                if (webRoot != null && webRoot.contains("WEB-INF")) {
                    webRoot = webRoot.substring(0, webRoot.lastIndexOf("WEB-INF"));
                }
            }

            return webRoot;
        } catch (Exception e) {
            ;
        }

        // 如果上面的方法仍无法获取Web目录，以防万一返回一个当前文件路径
        return (String) invokeHandler("getContextPath");
    }

    public WeakReference<Object> getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(WeakReference<Object> requestReference) {
        this.requestReference = requestReference;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private void setMethod(Class cls, Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = cls.getMethod("getMethod");
        method.setAccessible(true);
        setMethod((String) method.invoke(obj));
    }

    public String getProtocol() {
        if (null == protocol) {
            try {
                this.setProtocol(classOfHttpRequest, requestReference.get());
            } catch (InvocationTargetException e) {
                protocol = new String();
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (IllegalAccessException e) {
                protocol = new String();
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (NoSuchMethodException e) {
                protocol = new String();
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    private void setProtocol(Class cls, Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = cls.getMethod("getProtocol");
        method.setAccessible(true);
        setProtocol((String) method.invoke(obj));
    }

    public String getScheme() {
        if (scheme == null) {
            try {
                this.setScheme(classOfHttpRequest, requestReference.get());
            } catch (InvocationTargetException e) {
                scheme = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (IllegalAccessException e) {
                scheme = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (NoSuchMethodException e) {
                scheme = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    private void setScheme(Class cls, Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = cls.getMethod("getScheme");
        method.setAccessible(true);
        setScheme((String) method.invoke(obj));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestURI() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestURL() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrl(StringBuffer url) {
        this.setUrl(url.toString());
    }

    public String getQueryString() {
        if (this.queryString == null) {
            try {
                Method method = classOfHttpRequest.getMethod("getQueryString");
                method.setAccessible(true);
                this.setQueryString((String) method.invoke(requestReference.get()));
            } catch (IllegalAccessException e) {
                this.queryString = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (InvocationTargetException e) {
                this.queryString = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (NoSuchMethodException e) {
                this.queryString = "";
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String[]> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String[]> headers) {
        this.headers = headers;
    }

    public String getContextPath() {
        if (null == contextPath) {
            setContextPath();
        }
        return contextPath;
    }

    public void setContextPath() {
        this.contextPath = getDocumentRootPath(requestReference.get());
        if (this.contextPath == null || "/".equals(this.contextPath)) {
            this.contextPath = "ROOT";
        } else {
            String[] pathTokens = this.contextPath.split("/");
            this.contextPath = pathTokens[pathTokens.length - 1];
        }
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean isParsedParameters() {
        return parsedParameters;
    }

    public boolean isParsedMultipartParameters() {
        return parsedMultipartParameters;
    }

    public void setParsedMultipartParameters(boolean parsedMultipartParameters) {
        this.parsedMultipartParameters = parsedMultipartParameters;
    }

    public boolean isAnalyzing() {
        return analyzing;
    }

    public void setAnalyzing(boolean analyzing) {
        this.analyzing = analyzing;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }

    public void setCachedBody(byte[] cachedBody) {
        this.cachedBody = cachedBody;
    }

    public String getCachedBodyStr() {
        return cachedBodyStr;
    }

    public void setCachedBodyStr(String cachedBodyStr) {
        this.cachedBodyStr = cachedBodyStr;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public String getCachedContentType() {
        return cachedContentType;
    }

    public void setCachedContentType(String cachedContentType) {
        this.cachedContentType = cachedContentType;
    }

    public Integer getCachedContentLength() {
        return cachedContentLength;
    }

    public void setCachedContentLength(Integer cachedContentLength) {
        this.cachedContentLength = cachedContentLength;
    }

    public boolean isCachedXForwardedFor() {
        return cachedXForwardedFor;
    }

    public void setCachedXForwardedFor(boolean cachedXForwardedFor) {
        this.cachedXForwardedFor = cachedXForwardedFor;
    }

    public String[] getCachedXForwardedForHeaders() {
        return cachedXForwardedForHeaders;
    }

    public void setCachedXForwardedForHeaders(String[] cachedXForwardedForHeaders) {
        this.cachedXForwardedForHeaders = cachedXForwardedForHeaders;
    }

    public boolean isCachedXForwardedForAsString() {
        return cachedXForwardedForAsString;
    }

    public void setCachedXForwardedForAsString(boolean cachedXForwardedForAsString) {
        this.cachedXForwardedForAsString = cachedXForwardedForAsString;
    }

    public String getCachedXForwardedForHeadersAsString() {
        return cachedXForwardedForHeadersAsString;
    }

    public void setCachedXForwardedForHeadersAsString(String cachedXForwardedForHeadersAsString) {
        this.cachedXForwardedForHeadersAsString = cachedXForwardedForHeadersAsString;
    }

    public boolean isCheckedForDeserializer() {
        return checkedForDeserializer;
    }

    public void setCheckedForDeserializer(boolean checkedForDeserializer) {
        this.checkedForDeserializer = checkedForDeserializer;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public void setHeaderNames(Enumeration headerNames) {
        this.headerNames = headerNames;
    }

    private void setHeaderNames(Class cls, Object obj) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = cls.getMethod("getHeaderNames");
        method.setAccessible(true);
        Object headers = method.invoke(obj);
        if (headers instanceof Enumeration) {
            setHeaderNames((Enumeration) headers);
        }
    }

    private String getHeader(String name) {
        String value = "";
        try {
            Object requestObj = this.requestReference.get();
            if (null != requestObj) {
                value = (String) classOfHttpRequest.getMethod("getHeader", String.class).invoke(
                        this.requestReference.get(), name
                );
            }
        } catch (IllegalAccessException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (InvocationTargetException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (NoSuchMethodException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return value;
    }

    public String getCookieValue() {
        return this.getHeader("Cookie");
    }

    public String getTraceId() {
        return this.getHeader("x-trace-id");
    }

    public String getHeadersValue() {
        if (this.headerValues == null) {
            StringBuilder sb = new StringBuilder();
            if (null == this.headerNames) {
                try {
                    setHeaderNames(classOfHttpRequest, requestReference.get());
                } catch (InvocationTargetException e) {
                    ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
                } catch (IllegalAccessException e) {
                    ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
                } catch (NoSuchMethodException e) {
                    ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
                }
            }
            if (this.headerNames != null) {
                while (this.headerNames.hasMoreElements()) {
                    String name = this.headerNames.nextElement();
                    String value = this.getHeader(name);
                    sb.append(name).append(":").append(value).append("\n");
                }
            }
            this.headerValues = sb.toString();
        }
        return headerValues;
    }

    public String getRemoteHost() {
        if (remoteHost == null) {
            Object obj = invokeHandler("getRemoteHost");
            if (null != obj) {
                this.setRemoteHost((String) obj);
            }
        }
        return remoteHost;
    }

    private Object invokeHandler(Class cls, Object obj, String methodName) {
        Method method = null;
        try {
            method = cls.getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(obj);
        } catch (NoSuchMethodException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (InvocationTargetException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return null;
    }

    private Object invokeHandler(Class cls, Object obj, String methodName, Class[] argumentClasses, Object[] arguments) {
        Method method = null;
        try {
            method = cls.getMethod(methodName, argumentClasses);
            method.setAccessible(true);
            return method.invoke(obj, arguments);
        } catch (NoSuchMethodException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (IllegalAccessException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        } catch (InvocationTargetException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }
        return null;
    }


    public Object invokeHandler(String methodName) {
        return invokeHandler(classOfHttpRequest, requestReference.get(), methodName);
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRemoteAddr() {
        if (remoteAddr == null) {
            Method method = null;
            try {
                method = classOfHttpRequest.getMethod("getRemoteAddr");
                method.setAccessible(true);
                this.setRemoteAddr((String) method.invoke(requestReference.get()));
            } catch (NoSuchMethodException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (IllegalAccessException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            } catch (InvocationTargetException e) {
                ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
            }
        }
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getServerName() {
        if (null == serverName) {
            this.setServerName((String) invokeHandler("getServerName"));
        }
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getServerPort() {
        if (-1 == serverPort) {
            this.setServerPort((Integer) invokeHandler("getServerPort"));
        }
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isNotRepeatRequest() {
        return null == getTraceId();
    }
}
