package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchDubbo implements DispatchPlugin {
    public static final String LEGACY_DUBBO_SYNC_HANDLER = " com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper".substring(1);
    public static final String LEGACY_DUBBO_EXCHANGE_HANDLER = " com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeHandler".substring(1);
    public static final String LEGACY_DUBBO_PROXY_HANDLER = " com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (LEGACY_DUBBO_SYNC_HANDLER.equals(className)) {
            classVisitor = new LegacyDubboSyncHandlerAdapter(classVisitor, context);
        } else if (LEGACY_DUBBO_EXCHANGE_HANDLER.equals(className)) {
            classVisitor = new LegacyDubboExchangeHandlerAdapter(classVisitor, context);
        } else if (LEGACY_DUBBO_PROXY_HANDLER.equals(className)) {
            classVisitor = new LegacyDubboProxyHandlerAdapter(classVisitor, context);
        }

        return classVisitor;
    }
}
