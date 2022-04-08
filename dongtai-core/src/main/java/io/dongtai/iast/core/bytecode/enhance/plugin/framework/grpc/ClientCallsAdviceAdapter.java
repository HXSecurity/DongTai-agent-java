package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class ClientCallsAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected ClientCallsAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(AsmUtils.api, methodVisitor, access, name, descriptor);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            newLocal(ASM_TYPE_OBJECT);
            dup();
            storeLocal(nextLocal - 1);
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            loadArg(3);
            loadLocal(nextLocal - 1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$blockingUnaryCall);
        }
    }
}
