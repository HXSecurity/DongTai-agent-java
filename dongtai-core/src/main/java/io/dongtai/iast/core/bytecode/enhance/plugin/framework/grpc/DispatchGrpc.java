package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
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
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();
        if (classOfAbstractStub.equals(className)) {
            classVisitor = new AbstractStubAdapter(classVisitor, null);
        } else if (classOfClientCalls.equals(className)) {
            classVisitor = new ClientCallsAdapter(classVisitor, null);
        } else if (classOfAbstractServerImplBuilder.equals(className)) {
            classVisitor = new AbstractServerImplBuilderAdapter(classVisitor, null);
        } else if (classOfServerTransportListenerImpl.equals(className)) {
            classVisitor = new ServerTransportListenerImplAdapter(classVisitor, null);
        } else if (classOfServerStreamListenerImpl.equals(className)) {
            classVisitor = new ServerStreamListenerImplAdapter(classVisitor, null);
        } else if (classOfServerCallImpl.equals(className)) {
            classVisitor = new ServerCallImplAdapter(classVisitor, null);
        } else if (classOfByteString.equals(className)) {
            classVisitor = new ByteStringAdapter(classVisitor, null);
        }
        return classVisitor;
    }
}
