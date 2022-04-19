package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchGrpc implements DispatchPlugin {
    private final String classOfAbstractStub = "io.grpc.stub.AbstractStub";
    private final String classOfAbstractServerImplBuilder = "io.grpc.internal.AbstractServerImplBuilder";
    private final String classOfClientCalls = "io.grpc.stub.ClientCalls";
    private final String classOfServerTransportListenerImpl = "io.grpc.internal.ServerImpl$ServerTransportListenerImpl";
    private final String classOfServerStreamListenerImpl = "io.grpc.internal.ServerCallImpl$ServerStreamListenerImpl";
    private final String classOfServerCallImpl = "io.grpc.internal.ServerCallImpl";
    private static final String classOfByteString = "com.google.protobuf.ByteString";

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        switch (className) {
            case classOfAbstractStub:
                classVisitor = new AbstractStubAdapter(classVisitor, null);
                break;
            case classOfClientCalls:
                classVisitor = new ClientCallsAdapter(classVisitor, null);
                break;
            case classOfAbstractServerImplBuilder:
                classVisitor = new AbstractServerImplBuilderAdapter(classVisitor, null);
                break;
            case classOfServerTransportListenerImpl:
                classVisitor = new ServerTransportListenerImplAdapter(classVisitor, null);
                break;
            case classOfServerStreamListenerImpl:
                classVisitor = new ServerStreamListenerImplAdapter(classVisitor, null);
                break;
            case classOfServerCallImpl:
                classVisitor = new ServerCallImplAdapter(classVisitor, null);
                break;
            case classOfByteString:
                classVisitor = new ByteStringAdapter(classVisitor, null);
                break;
            default:
                break;
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
