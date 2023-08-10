package java.lang.dongtai;

import java.net.InetSocketAddress;
import java.util.*;

public class NopSpy implements SpyDispatcher {
    @Override
    public void enterScope(int id) {
    }

    @Override
    public boolean inScope(int id) {
        return false;
    }

    @Override
    public boolean isFirstLevelScope(int id) {
        return false;
    }

    @Override
    public void leaveScope(int id) {
    }

    /**
     * mark for enter Http Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterHttp() {
    }

    /**
     * mark for leave Http Entry Point
     *
     * @param request
     * @param response HttpResponse Object for collect http response body.
     * @since 1.3.1
     */
    @Override
    public void leaveHttp(Object request, Object response) {
    }

    /**
     * Determines whether it is a layer 1 HTTP entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelHttp() {
        return false;
    }

    @Override
    public void collectHttpRequest(Object obj, Object req, Object resp, StringBuffer requestURL, String requestURI,
                                   String queryString, String method, String protocol, String scheme,
                                   String serverName, String contextPath, String remoteAddr,
                                   boolean isSecure, int serverPort, Enumeration<?> headerNames) {
    }

    @Override
    public void onServletInputStreamRead(int ret, String desc, Object stream, byte[] bs, int offset, int len) {
    }

    @Override
    public void collectHttpResponse(Object obj, Object req, Object resp, Collection<?> headerNames, int status) {
    }

    @Override
    public void onServletOutputStreamWrite(String desc, Object stream, int b, byte[] bs, int offset, int len) {
    }

    @Override
    public void onPrintWriterWrite(String desc, Object writer, int b, String s, char[] cs, int offset, int len) {
    }

    @Override
    public void enterDubbo() {
    }

    @Override
    public void leaveDubbo(Object channel, Object request) {
    }

    @Override
    public boolean isFirstLevelDubbo() {
        return false;
    }

    @Override
    public void collectDubboRequest(Object handler, Object channel, Object request,
                                    String url, InetSocketAddress remoteAddress,
                                    boolean isTwoWay, boolean isEvent, boolean isBroken, boolean isHeartbeat) {
    }

    @Override
    public void collectDubboRequestSource(Object handler, Object invocation, String methodName,
                                          Object[] arguments, Class<?>[] argumentTypes, Map<String, ?> headers,
                                          String hookClass, String hookMethod, String hookSign) {
    }

    @Override
    public void collectDubboResponse(Object result, byte status) {
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSource() {

    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSource() {

    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSource() {
        return false;
    }

    /**
     * mark for enter Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterPropagator(boolean skipScope) {

    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leavePropagator(boolean skipScope) {

    }

    /**
     * Determines whether it is a layer 1 Propagator entry
     *
     * @return true if is a layer 1 Propagator entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelPropagator() {
        return false;
    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterSink() {

    }

    /**
     * mark for enter Sink Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leaveSink() {

    }

    /**
     *
     */
    @Override
    public boolean enterValidator() {
        return false;
    }

    /**
     * Determines whether it is a layer 1 Sink entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSink() {
        return false;
    }

    @Override
    public void enterIgnoreInternal() {
    }

    @Override
    public void leaveIgnoreInternal() {
    }

    @Override
    public void reportService(String category, String type, String host, String port, String handler) {

    }

    @Override
    public boolean isReplayRequest() {
        return false;
    }

    @Override
    public boolean isNotReplayRequest() {
        return false;
    }

    /**
     * mark for enter Source Entry Point
     *
     * @param instance       current class install object value, null if static class
     * @param argumentArray
     * @param retValue
     * @param framework
     * @param className
     * @param matchClassName
     * @param methodName
     * @param signCode
     * @param isStatic
     * @param handlerType
     * @return false if normal else throw a exception
     * @since 1.3.1
     */
    @Override
    public boolean collectMethodPool(Object instance, Object[] argumentArray, Object retValue, String framework, String className, String matchClassName, String methodName, String signCode, boolean isStatic, int handlerType) {
        return false;
    }

    @Override
    public boolean collectMethod(Object instance, Object[] parameters, Object retObject, String methodMatcher,
                                 String className, String matchedClassName, String methodName, String signature,
                                 boolean isStatic) {
        return false;
    }

    @Override
    public boolean traceFeignInvoke(Object instance, Object[] parameters,
                                    String className, String methodName, String signature) {
        return false;
    }

    @Override
    public boolean traceDubboInvoke(Object instance, String url, Object invocation, Object[] arguments,
                                    Map<String, String> headers, String className, String methodName,
                                    String signature) {
        return false;
    }

    @Override
    public boolean isSkipCollectDubbo(Object invocation) {
        return false;
    }

    @Override
    public boolean isSkipCollectFeign(Object instance) {
        return false;
    }

    @Override
    public boolean skipCollect(Object instance, Object[] parameters, Object retObject, String methodMatcher, String className, String matchedClassName, String methodName, String signature, boolean isStatic) {
        return false;
    }

}
