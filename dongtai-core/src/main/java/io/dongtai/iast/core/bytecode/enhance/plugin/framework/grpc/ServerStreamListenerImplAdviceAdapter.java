package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class ServerStreamListenerImplAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected ServerStreamListenerImplAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(AsmUtils.api, methodVisitor, access, name, descriptor);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$closeGrpcCall);
        }
    }
}
