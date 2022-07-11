package java.lang.dongtai;

public class NopSpy implements SpyDispatcher {
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

    /**
     * clone request object for copy http post body.
     *
     * @param req       HttpRequest Object
     * @param isJakarta true if jakarta-servlet-api else false
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneRequest(Object req, boolean isJakarta) {
        return null;
    }

    /**
     * clone response object for copy http response data.
     *
     * @param res
     * @param isJakarta
     * @return
     * @since 1.3.1
     */
    @Override
    public Object cloneResponse(Object res, boolean isJakarta) {
        return null;
    }

    /**
     * mark for enter Dubbo Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void enterDubbo() {

    }

    /**
     * mark for leave Dubbo Entry Point
     *
     * @param invocation
     * @param rpcResult
     * @since 1.3.1
     */
    @Override
    public void leaveDubbo(Object invocation, Object rpcResult) {

    }

    /**
     * Determines whether it is a layer 1 Dubbo entry
     *
     * @return true if is a layer 1 Dubbo entry; else false
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelDubbo() {
        return false;
    }

    @Override
    public void enterKafka(Object record) {

    }

    @Override
    public void kafkaBeforeSend(Object record) {

    }

    @Override
    public void kafkaAfterPoll(Object record) {

    }

    @Override
    public void leaveKafka() {

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
    public void enterPropagator() {

    }

    /**
     * mark for leave Source Entry Point
     *
     * @since 1.3.1
     */
    @Override
    public void leavePropagator() {

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
     * Determines whether it is a layer 1 Sink entry
     *
     * @return
     * @since 1.3.1
     */
    @Override
    public boolean isFirstLevelSink() {
        return false;
    }

    /**
     * @param channel
     * @since 1.4.0
     */
    @Override
    public Object clientInterceptor(Object channel) {
        return null;
    }

    @Override
    public Object serverInterceptor(Object serverServiceDefinition) {
        return null;
    }

    @Override
    public void startGrpcCall() {

    }

    @Override
    public void closeGrpcCall() {

    }

    @Override
    public void blockingUnaryCall(Object req, Object res) {

    }

    @Override
    public void sendMessage(Object message) {

    }

    @Override
    public void toStringUtf8(Object value) {

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
}
