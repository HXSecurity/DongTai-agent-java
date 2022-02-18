package io.dongtai.iast.core.bytecode.enhance.asm;

import java.lang.dongtai.SpyDispatcher;
import java.lang.dongtai.SpyDispatcherHandler;
import org.objectweb.asm.commons.Method;

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
    Method SPY$enterDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterDubbo"
    );
    Method SPY$leaveDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveDubbo"
    );
    Method SPY$isFirstLevelDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelDubbo"
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
            "enterPropagator"
    );
    Method SPY$leavePropagator = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leavePropagator"
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
}
