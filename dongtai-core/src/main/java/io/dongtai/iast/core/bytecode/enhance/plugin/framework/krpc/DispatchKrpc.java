package io.dongtai.iast.core.bytecode.enhance.plugin.framework.krpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchKrpc implements DispatchPlugin {

    static final String CLASS_OF_KRPC = " krpc.rpc.impl.RpcCallableBase".substring(1);
    static final String CLASS_OF_KRPC_API = " krpc.rpc.web.impl.DefaultWebRouteService".substring(1);
    static final String CLASS_OF_KRPC_HTTP = " krpc.rpc.web.impl.WebServer".substring(1);

    /**
     * 分发类访问器
     *
     * @param classVisitor 当前类的类访问器
     * @param context      当前类的上下文对象
     * @return ClassVisitor 命中的类访问起
     */
    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        if (context.getClassName().endsWith(CLASS_OF_KRPC)) {
            context.setMatchClassName(context.getClassName());
            classVisitor = new KrpcAdapter(classVisitor, context);
        }
        if (context.getClassName().endsWith(CLASS_OF_KRPC_API)) {
            context.setMatchClassName(context.getClassName());
            classVisitor = new KrpcAPIAdapter(classVisitor, context);
        }
        if (context.getClassName().endsWith(CLASS_OF_KRPC_HTTP)) {
            context.setMatchClassName(context.getClassName());
            classVisitor = new KrpcHttpAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    /**
     * 判断是否命中当前插件，如果命中则返回命中插件的类名，否则返回null
     *
     * @return String 命中插件的类的全限定类名
     */
    @Override
    public String isMatch() {
        return null;
    }
}
