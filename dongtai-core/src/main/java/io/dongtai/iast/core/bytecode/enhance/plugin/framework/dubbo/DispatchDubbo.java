package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchDubbo implements DispatchPlugin {
    public static final String ALIBABA_DUBBO_SYNC_HANDLER = " com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper".substring(1);
    public static final String APACHE_DUBBO_SYNC_HANDLER = " org.apache.dubbo.rpc.listener.ListenerInvokerWrapper".substring(1);
    public static final String ALIBABA_DUBBO_EXCHANGE_HANDLER = " com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeHandler".substring(1);
    public static final String APACHE_DUBBO_EXCHANGE_HANDLER = " org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeHandler".substring(1);
    public static final String APACHE_DUBBO_EXCHANGE_CHANNEL = " org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeChannel".substring(1);
    public static final String ALIBABA_DUBBO_PROXY_HANDLER = " com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker".substring(1);
    public static final String APACHE_DUBBO_PROXY_HANDLER = " org.apache.dubbo.rpc.proxy.AbstractProxyInvoker".substring(1);

    //com.caucho.hessian.client.HessianProxy.sendRequest(java.lang.String,java.lang.Object[])
    public static final String DUBBO_PROXY_HESSIAN = " com.caucho.hessian.client.HessianProxy".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (ALIBABA_DUBBO_SYNC_HANDLER.equals(className)) {
            classVisitor = new DubboSyncHandlerAdapter(classVisitor, context, " com.alibaba".substring(1));
        } else if (APACHE_DUBBO_SYNC_HANDLER.equals(className)) {
            classVisitor = new DubboSyncHandlerAdapter(classVisitor, context, " org.apache".substring(1));
        } else if (ALIBABA_DUBBO_EXCHANGE_HANDLER.equals(className)) {
            classVisitor = new DubboExchangeHandlerAdapter(classVisitor, context, " com.alibaba".substring(1));
        } else if (APACHE_DUBBO_EXCHANGE_HANDLER.equals(className)) {
            classVisitor = new DubboExchangeHandlerAdapter(classVisitor, context, " org.apache".substring(1));
        } else if (APACHE_DUBBO_EXCHANGE_CHANNEL.equals(className)) {
            classVisitor = new DubboExchangeChannelAdapter(classVisitor, context, " org.apache".substring(1));
        } else if (ALIBABA_DUBBO_PROXY_HANDLER.equals(className)) {
            classVisitor = new DubboProxyHandlerAdapter(classVisitor, context, " com.alibaba".substring(1));
        } else if (APACHE_DUBBO_PROXY_HANDLER.equals(className)) {
            classVisitor = new DubboProxyHandlerAdapter(classVisitor, context, " org.apache".substring(1));
        }
        if (DUBBO_PROXY_HESSIAN.equals(className)){
            System.out.println("dispatch" + className);
            classVisitor = new DubboHessianAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String getName() {
        return "dubbo";
    }
}
