package java.lang.dongtai;

public interface SpyDispatcher {

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

    /**
     * clone request object for copy http post body.
     *
     * @param req       HttpRequest Object
     * @param isJakarta true if jakarta-servlet-api else false
     * @return
     * @since 1.3.1
     */
    Object cloneRequest(Object req, boolean isJakarta);

    /**
     * clone response object for copy http response data.
     *
     * @param res
     * @param isJakarta
     * @return
     * @since 1.3.1
     */
    Object cloneResponse(Object res, boolean isJakarta);

    /**
     * mark for enter Dubbo Entry Point
     *
     * @since 1.3.1
     */
    void enterDubbo();

    /**
     * mark for leave Dubbo Entry Point
     *
     * @since 1.3.1
     */
    void leaveDubbo();

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    boolean isFirstLevelDubbo();

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
    void enterPropagator();

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    void leavePropagator();

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
}
