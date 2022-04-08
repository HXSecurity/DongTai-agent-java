package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class AbstractStubAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {

    protected AbstractStubAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(AsmUtils.api, methodVisitor, access, name, descriptor);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        // todo: wrapper channel with DongTaiClientCall
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$clientInterceptor);
        mv.visitTypeInsn(192, "io/grpc/Channel");
        storeArg(0);
    }
}
