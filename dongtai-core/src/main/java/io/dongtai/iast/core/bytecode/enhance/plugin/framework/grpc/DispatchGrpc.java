package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchGrpc implements DispatchPlugin {
    private final String classOfAbstractStub = "io.grpc.stub.AbstractStub";
    private final String classOfAbstractServerImplBuilder = "io.grpc.internal.AbstractServerImplBuilder";
    private final String classOfClientCalls = "io.grpc.stub.ClientCalls";
    private final String classOfServerTransportListenerImpl = "io.grpc.internal.ServerImpl$ServerTransportListenerImpl";
    private final String classOfMethodDescriptor = "io.grpc.MethodDescriptor";
    private final String classOfServerStreamListenerImpl = "io.grpc.internal.ServerCallImpl$ServerStreamListenerImpl";
    //io.grpc.internal.ServerCallImpl.ServerStreamListenerImpl.closed
    // io.grpc.MethodDescriptor.parseRequest

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
            case classOfServerTransportListenerImpl:
                classVisitor = new ServerTransportListenerImplAdapter(classVisitor, null);
                break;
            case classOfServerStreamListenerImpl:
                classVisitor = new ServerStreamListenerImplAdapter(classVisitor, null);
                break;
//            case classOfMethodDescriptor:
//                classVisitor = new MethodDescriptorAdapter(classVisitor, null);
//                break;
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
