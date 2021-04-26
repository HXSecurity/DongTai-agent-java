package com.secnium.iast.core.enhance.plugins.sources.servlet;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletStreamAdviceAdapter extends AbstractAdviceAdapter {

    public ServletStreamAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String desc, IastContext context, String signature) {
        super(methodVisitor, access, name, desc, context, "ServletRequest", signature);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterSource);
    }

    /**
     * servlet的流数据方法返回时，需要复制返回值并返回新的返回值
     *
     * @param opcode
     */
    @Override
    protected void after(final int opcode) {
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
