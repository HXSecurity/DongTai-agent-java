package io.dongtai.iast.core.bytecode.enhance.asm;

import org.objectweb.asm.commons.Method;

import java.lang.dongtai.SpyDispatcher;
import java.lang.dongtai.SpyDispatcherHandler;
import java.net.InetSocketAddress;
import java.util.*;

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

    Method SPY$enterScope = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterScope",
            int.class
    );
    Method SPY$inScope = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "inScope",
            int.class
    );
    Method SPY$isFirstLevelScope = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelScope",
            int.class
    );
    Method SPY$leaveScope = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveScope",
            int.class
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

    Method SPY$onServletInputStreamRead = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "onServletInputStreamRead",
            int.class,
            String.class,
            Object.class,
            byte[].class,
            int.class,
            int.class
    );

    Method SPY$collectHttpResponse = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectHttpResponse",
            Object.class,
            Object.class,
            Object.class,
            Collection.class,
            int.class
    );

    Method SPY$onServletOutputStreamWrite = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "onServletOutputStreamWrite",
            String.class,
            Object.class,
            int.class,
            byte[].class,
            int.class,
            int.class
    );

    Method SPY$onPrintWriterWrite = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "onPrintWriterWrite",
            String.class,
            Object.class,
            int.class,
            String.class,
            char[].class,
            int.class,
            int.class
    );

    Method SPY$enterDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterDubbo"
    );
    Method SPY$leaveDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveDubbo",
            Object.class,
            Object.class
    );
    Method SPY$isFirstLevelDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isFirstLevelDubbo"
    );
    Method SPY$collectDubboRequest = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectDubboRequest",
            Object.class,
            Object.class,
            Object.class,
            String.class,
            InetSocketAddress.class,
            boolean.class,
            boolean.class,
            boolean.class,
            boolean.class
    );

    Method SPY$collectDubboRequestSource = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectDubboRequestSource",
            Object.class,
            Object.class,
            String.class,
            Object[].class,
            Class[].class,
            Map.class,
            String.class,
            String.class,
            String.class
    );

    Method SPY$collectDubboResponse = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "collectDubboResponse",
            Object.class,
            byte.class
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

    Method SPY$enterValidator = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterValidator"
    );

    Method SPY$enterIgnoreInternal = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "enterIgnoreInternal"
    );
    Method SPY$leaveIgnoreInternal = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "leaveIgnoreInternal"
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

    Method SPY$skipCollect = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "skipCollect",
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

    Method SPY$traceDubboInvoke = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "traceDubboInvoke",
            Object.class,
            String.class,
            Object.class,
            Object[].class,
            Map.class,
            String.class,
            String.class,
            String.class
    );

    Method SPY$isSkipCollectDubbo = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isSkipCollectDubbo",
            Object.class
    );

    Method SPY$isSkipCollectFeign = InnerHelper.getAsmMethod(
            SpyDispatcher.class,
            "isSkipCollectFeign",
            Object.class
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
