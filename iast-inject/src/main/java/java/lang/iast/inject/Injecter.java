package java.lang.iast.inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 间谍类，藏匿在各个ClassLoader中
 * <p>
 * 从{@code 1.1.0}版本之后，修复了命名空间在Spy中不支持的问题
 */
public class Injecter {

    /**
     * 控制Spy是否在发生异常时主动对外抛出 T:主动对外抛出，会中断方法 F:不对外抛出，只将异常信息打印出来
     */
    public static volatile boolean isSpyThrowException = false;

    private static final Class<Ret> SPY_RET_CLASS = Ret.class;

    private static MethodHook METHOD_HOOK_HANDLER;

    /**
     * 判断间谍类是否已经完成初始化
     *
     * @return TRUE:已完成初始化;FALSE:未完成初始化;
     */
    public static boolean isInit() {
        return null != METHOD_HOOK_HANDLER;
    }

    /**
     * 初始化间谍
     *
     * @param ON_BEFORE_METHOD ON_BEFORE 回调
     * @param ON_RETURN_METHOD ON_RETURN 回调
     * @param ON_THROWS_METHOD ON_THROWS 回调
     */
    public static void init(
            final Method ON_BEFORE_METHOD,
            final Method ON_RETURN_METHOD,
            final Method ON_THROWS_METHOD,
            final Method ENTER_PROPAGATOR,
            final Method LEAVE_PROPAGATOR,
            final Method IS_FIRST_LEVEL_PROPAGATOR,
            final Method ENTER_SOURCE,
            final Method LEAVE_SOURCE,
            final Method IS_FIRST_LEVEL_SOURCE,
            final Method ENTER_SINK,
            final Method LEAVE_SINK,
            final Method IS_FIRST_LEVEL_SINK,
            final Method ENTER_HTTP,
            final Method LEAVE_HTTP,
            final Method IS_FIRST_LEVEL_HTTP,
            final Method HAS_TAINT,
            final Method CLONE_REQUEST,
            final Method IS_REPLAY_REQUEST,
            final Method CLONE_RESPONSE,
            final Method ENTER_DUBBO,
            final Method LEAVE_DUBBO,
            final Method IS_FIRST_LEVEL_DUBBO
    ) {
        if (null == METHOD_HOOK_HANDLER) {
            return;
        }
        METHOD_HOOK_HANDLER = new MethodHook(
                ON_BEFORE_METHOD,
                ON_RETURN_METHOD,
                ON_THROWS_METHOD,
                ENTER_PROPAGATOR,
                LEAVE_PROPAGATOR,
                IS_FIRST_LEVEL_PROPAGATOR,
                ENTER_SOURCE,
                LEAVE_SOURCE,
                IS_FIRST_LEVEL_SOURCE,
                ENTER_SINK,
                LEAVE_SINK,
                IS_FIRST_LEVEL_SINK,
                ENTER_HTTP,
                LEAVE_HTTP,
                IS_FIRST_LEVEL_HTTP,
                HAS_TAINT,
                CLONE_REQUEST,
                IS_REPLAY_REQUEST,
                CLONE_RESPONSE,
                ENTER_DUBBO,
                LEAVE_DUBBO,
                IS_FIRST_LEVEL_DUBBO
        );
    }

    /**
     * 清理间谍钩子方法
     *
     */
    public static void clean() {
        METHOD_HOOK_HANDLER = null;
    }


    private static void handleException(Throwable cause) throws Throwable {
        if (isSpyThrowException) {
            throw cause;
        }  //cause.printStackTrace();

    }


    public static void spyMethodOnBefore(final Object retValue,
            final Object[] argumentArray,
            final String framework,
            final String javaClassName,
            final String matchClassName,
            final String javaMethodName,
            final String javaMethodDesc,
            final Object target,
            final String signCode,
            final boolean isStatic,
            final int METHOD_HOOK_HANDLERType) throws Throwable {
        try {
            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ON_BEFORE_METHOD
                        .invoke(null, framework, javaClassName, matchClassName, javaMethodName, javaMethodDesc,
                                target, argumentArray, retValue, signCode, isStatic, METHOD_HOOK_HANDLERType);
            }
        } catch (Throwable cause) {
            handleException(cause);
        }
    }

    public static void spyMethodOnReturn(final Object retValue,
            final int listenerId
    ) throws Throwable {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ON_RETURN_METHOD.invoke(null, listenerId, SPY_RET_CLASS, retValue);
            }
        } catch (Throwable cause) {
            handleException(cause);
        }
    }

    public static Ret spyMethodOnThrows(final Throwable throwable,
            final int listenerId) throws Throwable {
        try {

            if (null == METHOD_HOOK_HANDLER) {
                return Ret.RET_NONE;
            }
            return (Ret) METHOD_HOOK_HANDLER.ON_THROWS_METHOD.invoke(null, listenerId, SPY_RET_CLASS, throwable);
        } catch (Throwable cause) {
            handleException(cause);
            return Ret.RET_NONE;
        }
    }

    public static void spyMethodEnterPropagator() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ENTER_PROPAGATOR.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void spyMethodLeavePropagator() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.LEAVE_PROPAGATOR.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirstLevelPropagator() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_TOP_LEVEL_PROPAGATOR.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFirstLevelSink() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_TOP_LEVEL_SINK.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean hasTaint() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.HAS_TAINT.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void enterSink() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ENTER_SINK.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void leaveSink() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.LEAVE_SINK.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirstLevelSource() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_TOP_LEVEL_SOURCE.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void enterSource() {
        try {
            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ENTER_SOURCE.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void leaveSource() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.LEAVE_SOURCE.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void enterHttp() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ENTER_HTTP.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void leaveHttp(final Object response) {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.LEAVE_HTTP.invoke(null, response);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirstLevelHttp() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_TOP_LEVEL_HTTP.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 克隆Request对象
     *
     * @param req
     * @return
     * @throws Throwable
     */
    public static Object cloneRequest(Object req, boolean isJakarta) throws Throwable {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return METHOD_HOOK_HANDLER.CLONE_REQUEST.invoke(null, req, isJakarta);
            }
        } catch (Throwable cause) {
            handleException(cause);
        }
        return req;
    }

    public static boolean isReplayRequest() throws Throwable {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_REPLAY_REQUEST.invoke(null);
            }
        } catch (Throwable cause) {
            handleException(cause);
        }
        return false;
    }

    /**
     * 克隆Response对象
     *
     * @param response
     * @return
     * @throws Throwable
     */
    public static Object cloneResponse(Object response, boolean isJakarta) throws Throwable {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return METHOD_HOOK_HANDLER.CLONE_RESPONSE.invoke(null, response, isJakarta);
            }
        } catch (Throwable cause) {
            handleException(cause);
        }
        return response;
    }

    /**
     * 返回结果
     */
    public static class Ret {

        public static final int RET_STATE_NONE = 0;
        public static final int RET_STATE_RETURN = 1;
        public static final int RET_STATE_THROWS = 2;
        private static final Ret RET_NONE = new Ret(RET_STATE_NONE, null);
        /**
         * 返回状态(0:NONE;1:RETURN;2:THROWS)
         */
        public final int state;
        /**
         * 应答对象
         */
        public final Object respond;

        /**
         * 构造返回结果
         *
         * @param state   返回状态
         * @param respond 应答对象
         */
        private Ret(int state, Object respond) {
            this.state = state;
            this.respond = respond;
        }

        public static Ret newInstanceForNone() {
            return RET_NONE;
        }

        public static Ret newInstanceForReturn(Object object) {
            return new Ret(RET_STATE_RETURN, object);
        }

        public static Ret newInstanceForThrows(Throwable throwable) {
            return new Ret(RET_STATE_THROWS, throwable);
        }

    }

    /**
     * 回调方法钩子
     */
    public static class MethodHook {

        private final Method ON_BEFORE_METHOD;
        private final Method ON_RETURN_METHOD;
        private final Method ON_THROWS_METHOD;
        private final Method ENTER_PROPAGATOR;
        private final Method LEAVE_PROPAGATOR;
        private final Method IS_TOP_LEVEL_PROPAGATOR;
        private final Method ENTER_SOURCE;
        private final Method LEAVE_SOURCE;
        private final Method IS_TOP_LEVEL_SOURCE;
        private final Method ENTER_SINK;
        private final Method LEAVE_SINK;
        private final Method IS_TOP_LEVEL_SINK;
        private final Method ENTER_HTTP;
        private final Method LEAVE_HTTP;
        private final Method IS_TOP_LEVEL_HTTP;
        private final Method HAS_TAINT;
        private final Method CLONE_REQUEST;
        private final Method IS_REPLAY_REQUEST;
        private final Method CLONE_RESPONSE;
        final Method ENTER_DUBBO;
        final Method LEAVE_DUBBO;
        final Method IS_FIRST_LEVEL_DUBBO;

        public MethodHook(final Method on_before_method,
                final Method on_return_method,
                final Method on_throws_method,
                final Method ENTER_PROPAGATOR,
                final Method LEAVE_PROPAGATOR,
                final Method IS_TOP_LEVEL_PROPAGATOR,
                final Method ENTER_SOURCE,
                final Method LEAVE_SOURCE,
                final Method IS_TOP_LEVEL_SOURCE,
                final Method ENTER_SINK,
                final Method LEAVE_SINK,
                final Method IS_TOP_LEVEL_SINK,
                final Method ENTER_HTTP,
                final Method LEAVE_HTTP,
                final Method IS_TOP_LEVEL_HTTP,
                final Method HAS_TAINT,
                final Method CLONE_REQUEST,
                final Method IS_REPLAY_REQUEST,
                final Method CLONE_RESPONSE,
                final Method ENTER_DUBBO,
                final Method LEAVE_DUBBO,
                final Method IS_FIRST_LEVEL_DUBBO) {
            assert null != on_before_method;
            assert null != on_return_method;
            assert null != on_throws_method;
            this.ON_BEFORE_METHOD = on_before_method;
            this.ON_RETURN_METHOD = on_return_method;
            this.ON_THROWS_METHOD = on_throws_method;
            this.ENTER_PROPAGATOR = ENTER_PROPAGATOR;
            this.LEAVE_PROPAGATOR = LEAVE_PROPAGATOR;
            this.IS_TOP_LEVEL_PROPAGATOR = IS_TOP_LEVEL_PROPAGATOR;
            this.IS_TOP_LEVEL_SOURCE = IS_TOP_LEVEL_SOURCE;
            this.IS_TOP_LEVEL_SINK = IS_TOP_LEVEL_SINK;
            this.ENTER_SOURCE = ENTER_SOURCE;
            this.LEAVE_SOURCE = LEAVE_SOURCE;
            this.ENTER_SINK = ENTER_SINK;
            this.LEAVE_SINK = LEAVE_SINK;
            this.ENTER_HTTP = ENTER_HTTP;
            this.LEAVE_HTTP = LEAVE_HTTP;
            this.IS_TOP_LEVEL_HTTP = IS_TOP_LEVEL_HTTP;
            this.HAS_TAINT = HAS_TAINT;
            this.CLONE_REQUEST = CLONE_REQUEST;
            this.IS_REPLAY_REQUEST = IS_REPLAY_REQUEST;
            this.CLONE_RESPONSE = CLONE_RESPONSE;
            this.ENTER_DUBBO = ENTER_DUBBO;
            this.LEAVE_DUBBO = LEAVE_DUBBO;
            this.IS_FIRST_LEVEL_DUBBO = IS_FIRST_LEVEL_DUBBO;
        }
    }

    /**
     * enter Dubbo Method
     *
     * @since 1.2.0
     */
    public static void enterDubbo() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.ENTER_DUBBO.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * leave Dubbo Method
     *
     * @since 1.2.0
     */
    public static void leaveDubbo() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                METHOD_HOOK_HANDLER.LEAVE_DUBBO.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirstLevelDubbo() {
        try {

            if (null != METHOD_HOOK_HANDLER) {
                return (Boolean) METHOD_HOOK_HANDLER.IS_FIRST_LEVEL_DUBBO.invoke(null);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

}
