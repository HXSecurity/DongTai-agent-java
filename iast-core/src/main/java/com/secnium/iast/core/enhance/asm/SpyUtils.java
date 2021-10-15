package com.secnium.iast.core.enhance.asm;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.EventListenerHandlers;
import com.secnium.iast.core.handler.controller.TrackerHelper;

import java.lang.iast.inject.Injecter;

import static com.secnium.iast.core.enhance.asm.SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod;

/**
 * Spy类操作工具类
 *
 * @author luajia@taobao.com
 */
public class SpyUtils {


    /**
     * 初始化Spy类
     *
     * @param namespace 命名空间
     */
    public synchronized static void init(final String namespace) {
        EngineManager.SCOPE_TRACKER.set(new TrackerHelper());
        // 注册接口单例对象，将各模块的实现类传递进去
        if (Injecter.isInit(namespace)) {
            return;
        }

        Injecter.init(
                namespace,
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onBefore",
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        Object.class,
                        Object[].class,
                        Object.class,
                        String.class,
                        boolean.class,
                        int.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onReturn",
                        int.class,
                        Class.class,
                        Object.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "onThrows",
                        int.class,
                        Class.class,
                        Throwable.class
                ),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "enterPropagator"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "leavePropagator"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "isFirstLevelPropagator"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "enterSource"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "leaveSource"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "isFirstLevelSource"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "enterSink"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "leaveSink"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "isFirstLevelSink"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "enterHttp"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "leaveHttp", Object.class),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "isFirstLevelHttp"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "hasTaintValue"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "cloneRequest", Object.class, boolean.class),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "isReplayRequest"),
                unCaughtGetClassDeclaredJavaMethod(EventListenerHandlers.class, "cloneResponse", Object.class, boolean.class)
        );
    }

    /**
     * 清理Spy中的命名空间
     *
     * @param namespace 命名空间
     */
    public synchronized static void clean(final String namespace) {
        Injecter.clean(namespace);
    }

}
