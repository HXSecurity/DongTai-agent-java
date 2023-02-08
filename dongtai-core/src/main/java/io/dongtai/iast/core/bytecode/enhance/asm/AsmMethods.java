package io.dongtai.iast.core.bytecode.enhance.asm;

import org.objectweb.asm.commons.Method;

import java.lang.dongtai.SpyDispatcher;
import java.lang.dongtai.SpyDispatcherHandler;
import java.util.Enumeration;

/**
 * 常用的ASM method 集合 省得我到处声明
 *
 * @author luanjia@taobao.com
 * @date 16/5/21 Modified by dongzhiyong@huoxian.cn
 */
public interface AsmMethods {

    class InnerHelper {

        private InnerHelper() {
        }

        static Method getAsmMethod(final Class<?> clazz,
                                   final String methodName,
                                   final Class<?>... parameterClassArray) {
            return Method.getMethod(
                    SandboxReflectUtils.unCaughtGetClassDeclaredJavaMethod(clazz, methodName, parameterClassArray));
        }
    }

    Method SPY_HANDLER$getDispatcher = InnerHelper.getAsmMethod(
            SpyDispatcherHandler.class,
            "getDispatcher"
    );
    Method SPY$enterHttp = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterHttp"
    );
    Method SPY$leaveHttp = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveHttp",
            Object.class,
            Object.class
    );
    Method SPY$isFirstLevelHttp = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelHttp"
    );
    Method SPY$collectHttpRequest = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectHttpRequest",
            Object.class,
            Object.class,
            Object.class,
            StringBuffer.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            boolean.class,
            int.class,
            Enumeration.class
    );

    Method SPY$cloneRequest = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "cloneRequest",
            Object.class,
            boolean.class
    );
    Method SPY$cloneResponse = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "cloneResponse",
            Object.class,
            boolean.class
    );

    Method SPY$enterSource = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterSource"
    );
    Method SPY$leaveSource = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveSource"
    );
    Method SPY$isFirstLevelSource = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelSource"
    );
    Method SPY$enterPropagator = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterPropagator",
            boolean.class
    );
    Method SPY$leavePropagator = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leavePropagator",
            boolean.class
    );
    Method SPY$isFirstLevelPropagator = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelPropagator"
    );
    Method SPY$enterSink = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterSink"
    );
    Method SPY$leaveSink = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveSink"
    );
    Method SPY$isFirstLevelSink = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelSink"
    );
    Method SPY$collectMethodPool = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectMethodPool",
            Object.class,
            Object[].class,
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            boolean.class,
            int.class
    );

    Method SPY$collectMethod = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectMethod",
            Object.class,
            Object[].class,
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            boolean.class
    );

    Method SPY$traceFeignInvoke = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "traceFeignInvoke",
            Object.class,
            Object[].class,
            String.class,
            String.class,
            String.class
    );

    Method SPY$reportService = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "reportService",
            String.class,
            String.class,
            String.class,
            String.class,
            String.class
    );
    Method SPY$isReplayRequest = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isReplayRequest"
    );
    Method SPY$isNotReplayRequest = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isNotReplayRequest"
    );
}
