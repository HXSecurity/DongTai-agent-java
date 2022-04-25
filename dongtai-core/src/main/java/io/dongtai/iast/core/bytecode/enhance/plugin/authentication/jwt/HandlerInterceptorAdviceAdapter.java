package io.dongtai.iast.core.bytecode.enhance.plugin.authentication.jwt;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class HandlerInterceptorAdviceAdapter extends AbstractAdviceAdapter {

    public HandlerInterceptorAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context,
                                           String type, String signCode) {
        super(mv, access, name, desc, context, type, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        isReplayRequest();
        mv.visitJumpInsn(EQ, elseLabel);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        mv.visitInsn(ARETURN);
        mark(elseLabel);
        mark(endLabel);
    }

    @Override
    protected void after(int opcode) {
    }

    private void isReplayRequest() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isReplayRequest);
    }
}
