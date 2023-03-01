package java.lang.dongtai;

import java.util.*;

public interface SpyDispatcher {

    void enterScope(int id);

    boolean inScope(int id);

    boolean isFirstLevelScope(int id);

    void leaveScope(int id);

    /**
     * mark for enter Http Entry Point
     *
     * @since 1.3.1
     */
    void enterHttp();

    /**
     * mark for leave Http Entry Point
     *
     * @param response HttpResponse Object for collect http response body.
     * @since 1.3.1
     */
    void leaveHttp(final Object request, final Object response);

    /**
     * Determines whether it is a layer 1 HTTP entry
     *
     * @return
     * @since 1.3.1
     */
    boolean isFirstLevelHttp();

    void collectHttpRequest(Object obj, Object req, Object resp, StringBuffer requestURL, String requestURI,
                            String queryString, String method, String protocol, String scheme,
                            String serverName, String contextPath, String remoteAddr,
                            boolean isSecure, int serverPort, Enumeration<?> headerNames);

    void onServletInputStreamRead(int ret, String desc, Object stream, byte[] bs, int offset, int len);

    void collectHttpResponse(Object obj, Object req, Object resp, Collection<?> headers, int status);

    void onServletOutputStreamWrite(String desc, Object stream, int b, byte[] bs, int offset, int len);

    void onPrintWriterWrite(String desc, Object writer, int b, String s, char[] cs, int offset, int len);

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    void enterSource();

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    void leaveSource();

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    boolean isFirstLevelSource();

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    void enterPropagator(boolean skipScope);

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    void leavePropagator(boolean skipScope);

    /**
     * Determines whether it is a layer 1 Propagator entry
     *
     * @return true if is a layer 1 Propagator entry; else false
     * @since 1.3.1
     */
    boolean isFirstLevelPropagator();

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    void enterSink();

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    void leaveSink();

    /**
     * Determines whether it is a layer 1 Sink entry
     *
     * @return
     * @since 1.3.1
     */
    boolean isFirstLevelSink();

    void reportService(String category, String type, String host, String port, String handler);

    boolean isReplayRequest();

    boolean isNotReplayRequest();

    /**
     * mark for enter Source Entry Point
     *
     * @param retValue
     * @param argumentArray
     * @param framework
     * @param className
     * @param matchClassName
     * @param instance       current class install object value, null if static class
     * @param signCode
     * @param isStatic
     * @param handlerType
     * @return false if normal else throw a exception
     * @since 1.3.1
     */
    boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework,
                              String className, String matchClassName, String methodName, String signCode,
                              boolean isStatic, int handlerType);

    public boolean collectMethod(Object instance, Object[] parameters, Object retObject, String methodMatcher,
                                 String className, String matchedClassName, String methodName, String signature,
                                 boolean isStatic);

    boolean traceFeignInvoke(Object instance, Object[] parameters,
                             String className, String methodName, String signature);

    boolean traceDubboInvoke(Object instance, String url, Object invocation, Object[] arguments,
                             Map<String, String> headers, String className, String methodName,
                             String signature);
}
