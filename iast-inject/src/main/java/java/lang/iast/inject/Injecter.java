package java.lang.iast.inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 间谍类，藏匿在各个ClassLoader中
 * <p>
 * 从{@code 1.1.0}版本之后，修复了命名空间在Spy中不支持的问题
 */
public class Injecter {

    /**
     * 控制Spy是否在发生异常时主动对外抛出
     * T:主动对外抛出，会中断方法
     * F:不对外抛出，只将异常信息打印出来
     */
    public static volatile boolean isSpyThrowException = false;

    private static final Class<Ret> SPY_RET_CLASS = Ret.class;

    private static final Map<String, MethodHook> namespaceMethodHookMap
            = new ConcurrentHashMap<String, MethodHook>();

    /**
     * 判断间谍类是否已经完成初始化
     *
     * @param namespace 命名空间
     * @return TRUE:已完成初始化;FALSE:未完成初始化;
     */
    public static boolean isInit(final String namespace) {
        return namespaceMethodHookMap.containsKey(namespace);
    }

    /**
     * 初始化间谍
     *
     * @param namespace        命名空间
     * @param ON_BEFORE_METHOD ON_BEFORE 回调
     * @param ON_RETURN_METHOD ON_RETURN 回调
     * @param ON_THROWS_METHOD ON_THROWS 回调
     */
    public static void init(final String namespace,
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
                            final Method HAS_TAINT
    ) {
        namespaceMethodHookMap.put(
                namespace,
                new MethodHook(
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
                        HAS_TAINT
                )
        );
    }

    /**
     * 清理间谍钩子方法
     *
     * @param namespace 命名空间
     */
    public static void clean(final String namespace) {
        namespaceMethodHookMap.remove(namespace);

        // 如果是最后的一个命名空间，则需要重新清理Node中所持有的Thread
        if (namespaceMethodHookMap.isEmpty()) {
            selfCallBarrier.cleanAndInit();
        }

    }


    // 全局序列
    private static final AtomicInteger sequenceRef = new AtomicInteger(1000);

    /**
     * 生成全局唯一序列，
     * 在JVM-SANDBOX中允许多个命名空间的存在，不同的命名空间下listenerId/objectId将会被植入到同一份字节码中，
     * 此时需要用全局的ID生成策略规避不同的命名空间
     *
     * @return 全局自增序列
     */
    public static int nextSequence() {
        return sequenceRef.getAndIncrement();
    }


    private static void handleException(Throwable cause) throws Throwable {
        if (isSpyThrowException) {
            throw cause;
        }  //cause.printStackTrace();

    }

    private static final SelfCallBarrier selfCallBarrier = new SelfCallBarrier();


    public static void spyMethodOnBefore(final Object retValue,
                                         final Object[] argumentArray,
                                         final String namespace,
                                         final String framework,
                                         final int listenerId,
                                         final String javaClassName,
                                         final String javaMethodName,
                                         final String javaMethodDesc,
                                         final Object target,
                                         final String signCode,
                                         final boolean isStatic,
                                         final int hookType) throws Throwable {
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ON_BEFORE_METHOD.invoke(null,
                            listenerId, framework, javaClassName, javaMethodName, javaMethodDesc, target, argumentArray, retValue, signCode, isStatic, hookType);
                }
            } catch (Throwable cause) {
                handleException(cause);
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void spyMethodOnReturn(final Object retValue,
                                         final String namespace,
                                         final int listenerId
    ) throws Throwable {
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ON_RETURN_METHOD.invoke(null, listenerId, SPY_RET_CLASS, retValue);
                }
            } catch (Throwable cause) {
                handleException(cause);
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static Ret spyMethodOnThrows(final Throwable throwable,
                                        final String namespace,
                                        final int listenerId) throws Throwable {
        final Thread thread = Thread.currentThread();
        if (selfCallBarrier.isEnter(thread)) {
            return Ret.RET_NONE;
        }
        final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
        try {
            final MethodHook hook = namespaceMethodHookMap.get(namespace);
            if (null == hook) {
                return Ret.RET_NONE;
            }
            return (Ret) hook.ON_THROWS_METHOD.invoke(null, listenerId, SPY_RET_CLASS, throwable);
        } catch (Throwable cause) {
            handleException(cause);
            return Ret.RET_NONE;
        } finally {
            selfCallBarrier.exit(thread, node);
        }
    }

    public static void spyMethodEnterPropagator(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ENTER_PROPAGATOR.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void spyMethodLeavePropagator(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.LEAVE_PROPAGATOR.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static boolean isFirstLevelPropagator(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    return (Boolean) hook.IS_TOP_LEVEL_PROPAGATOR.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
        return false;
    }

    public static boolean isFirstLevelSink(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    return (Boolean) hook.IS_TOP_LEVEL_SINK.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
        return false;
    }

    public static boolean hasTaint(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    return (Boolean) hook.HAS_TAINT.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
        return false;
    }

    public static void enterSink(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ENTER_SINK.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void leaveSink(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.LEAVE_SINK.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static boolean isFirstLevelSource(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    return (Boolean) hook.IS_TOP_LEVEL_SOURCE.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
        return false;
    }

    public static void enterSource(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ENTER_SOURCE.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void leaveSource(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.LEAVE_SOURCE.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void enterHttp(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.ENTER_HTTP.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static void leaveHttp(final String namespace) {
        // 进入传播节点
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    hook.LEAVE_HTTP.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
    }

    public static boolean isFirstLevelHttp(final String namespace) {
        final Thread thread = Thread.currentThread();
        if (!selfCallBarrier.isEnter(thread)) {
            final SelfCallBarrier.Node node = selfCallBarrier.enter(thread);
            try {
                final MethodHook hook = namespaceMethodHookMap.get(namespace);
                if (null != hook) {
                    return (Boolean) hook.IS_TOP_LEVEL_HTTP.invoke(null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                selfCallBarrier.exit(thread, node);
            }
        }
        return false;
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
     * 本地线程
     */
    public static class SelfCallBarrier {

        public static class Node {
            private final Thread thread;
            private final ReentrantLock lock;
            private Node pre;
            private Node next;

            Node() {
                this(null);
            }

            Node(final Thread thread) {
                this(thread, null);
            }

            Node(final Thread thread, final ReentrantLock lock) {
                this.thread = thread;
                this.lock = lock;
            }

        }

        // 删除节点
        void delete(final Node node) {
            node.pre.next = node.next;
            if (null != node.next) {
                node.next.pre = node.pre;
            }
            // help gc
            node.pre = (node.next = null);
        }

        // 插入节点
        void insert(final Node top, final Node node) {
            if (null != top.next) {
                top.next.pre = node;
            }
            node.next = top.next;
            node.pre = top;
            top.next = node;
        }

        static final int THREAD_LOCAL_ARRAY_LENGTH = 512;

        final Node[] nodeArray = new Node[THREAD_LOCAL_ARRAY_LENGTH];

        SelfCallBarrier() {
            cleanAndInit();
        }

        Node createTopNode() {
            return new Node(null, new ReentrantLock());
        }

        void cleanAndInit() {
            for (int i = 0; i < THREAD_LOCAL_ARRAY_LENGTH; i++) {
                nodeArray[i] = createTopNode();
            }
        }

        int abs(int val) {
            return val < 0
                    ? val * -1
                    : val;
        }

        boolean isEnter(Thread thread) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            Node node = top;
            try {
                // spin for lock
                while (!top.lock.tryLock()) {
                }
                while (null != node.next) {
                    node = node.next;
                    if (thread == node.thread) {
                        return true;
                    }
                }
                return false;
            } finally {
                top.lock.unlock();
            }
        }

        Node enter(Thread thread) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            final Node node = new Node(thread);
            try {
                while (!top.lock.tryLock()) {
                }
                insert(top, node);
            } finally {
                top.lock.unlock();
            }
            return node;
        }

        void exit(Thread thread, Node node) {
            final Node top = nodeArray[abs(thread.hashCode()) % THREAD_LOCAL_ARRAY_LENGTH];
            try {
                while (!top.lock.tryLock()) {
                }
                delete(node);
            } finally {
                top.lock.unlock();
            }
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
                          final Method HAS_TAINT) {
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
        }
    }

}
