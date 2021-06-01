package com.secnium.iast.core.enhance.plugins.core.adapter;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SourceAdviceAdapter extends AbstractAdviceAdapter {

    public SourceAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String type, String signCode) {
        super(mv, access, name, desc, context, type, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterSource();
    }

    @Override
    protected void after(int opcode) {
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
