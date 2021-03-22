package com.secnium.iast.core.enhance.plugins.sources.servlet;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletAdviceAdapter extends AbstractAdviceAdapter {

    public ServletAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String desc, IASTContext context, String signature) {
        super(methodVisitor, access, name, desc, context, "ServletRequest", signature);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterSource();
    }

    @Override
    protected void after(final int opcode) {
        if (!isThrow(opcode)) {
            Label elseLabel = new Label();
            Label endLabel = new Label();

            isFirstLevelSource();
            mv.visitJumpInsn(EQ, elseLabel);

            captureMethodState(opcode, HookType.SOURCE.getValue(), true);

            mark(elseLabel);
            mark(endLabel);
        }
        leaveSource();
    }

    private void enterSource() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterSource);
    }

    private void leaveSource() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveSource);
    }

    private void isFirstLevelSource() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelSource);
    }
}
