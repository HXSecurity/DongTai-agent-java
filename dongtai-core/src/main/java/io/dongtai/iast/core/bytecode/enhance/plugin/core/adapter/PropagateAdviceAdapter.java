package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropagateAdviceAdapter extends AbstractAdviceAdapter {
    public PropagateAdviceAdapter(MethodVisitor mv, int access, String name, String desc, ClassContext context,
                                  String framework, String signCode) {
        super(mv, access, name, desc, context, framework, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterPropagator();
    }

    @Override
    protected void after(final int opcode) {
        if (!isThrow(opcode)) {
            Label elseLabel = new Label();
            Label endLabel = new Label();
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(signature);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelPropagator);
            mv.visitJumpInsn(EQ, elseLabel);
            captureMethodState(opcode, HookType.PROPAGATOR.getValue(), true);
            mark(elseLabel);
            mark(endLabel);
        }
        leavePropagator();
    }

    private void enterPropagator() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(signature);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterPropagator);
    }

    private void leavePropagator() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(signature);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leavePropagator);
    }
}
