package com.secnium.iast.core.enhance.plugins.sources.spring;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ArgumentResolverAdviceAdapter extends AbstractAdviceAdapter {
    public ArgumentResolverAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IASTContext context, String signCode) {
        super(mv, access, name, desc, context, "SpringArguments", signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterSource);
    }

    @Override
    protected void after(int opcode) {
        if (!isThrow(opcode)) {
            Label elseLabel = new Label();
            Label endLabel = new Label();

            push(context.getNamespace());
            invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelSource);
            mv.visitJumpInsn(EQ, elseLabel);

            captureMethodState(opcode, HookType.SOURCE.getValue(), true);

            mark(elseLabel);
            mark(endLabel);
        }
        leaveSource();
    }

    private void leaveSource() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveSource);
    }
}
