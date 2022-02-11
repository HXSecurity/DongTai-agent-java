package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class SourceAdviceAdapter extends AbstractAdviceAdapter {

    public SourceAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String type,
            String signCode) {
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
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterSource);
    }

    private void leaveSource() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveSource);
    }

    private void isFirstLevelSource() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelSource);
    }
}
