package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchGrpc implements DispatchPlugin {
    private final String classOfAbstractStub = "io.grpc.stub.AbstractStub";
    private final String classOfAbstractServerImplBuilder = "io.grpc.internal.AbstractServerImplBuilder";
    private final String classOfClientCalls = "io.grpc.stub.ClientCalls";

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        switch (className) {
            case classOfAbstractStub:
                context.setMatchClassName(className);
                classVisitor = new AbstractStubAdapter(classVisitor, null);
                break;
            case classOfClientCalls:
                context.setMatchClassName(className);
                break;
            case classOfAbstractServerImplBuilder:
                context.setMatchClassName(className);
                classVisitor = new AbstractServerImplBuilderAdapter(classVisitor, null);
                break;
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
