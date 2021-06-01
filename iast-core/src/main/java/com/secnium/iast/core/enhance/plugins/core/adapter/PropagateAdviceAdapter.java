package com.secnium.iast.core.enhance.plugins.core.adapter;

import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropagateAdviceAdapter extends AbstractAdviceAdapter {
    private static final boolean ENABLE_ALL_HOOK = PropertyUtils.getInstance().isEnableAllHook();

    public PropagateAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String framework, String signCode) {
        super(mv, access, name, desc, context, framework, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        if (!ENABLE_ALL_HOOK) {
            enterPropagator();
        }
    }

    @Override
    protected void after(final int opcode) {
        if (!isThrow(opcode)) {
            if (!ENABLE_ALL_HOOK) {
                Label elseLabel = new Label();
                Label endLabel = new Label();
                push(context.getNamespace());
                invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelPropagator);
                mv.visitJumpInsn(EQ, elseLabel);
                captureMethodState(opcode, HookType.PROPAGATOR.getValue(), true);
                mark(elseLabel);
                mark(endLabel);
            } else {
                captureMethodState(opcode, HookType.PROPAGATOR.getValue(), true);
            }
        }
        if (!ENABLE_ALL_HOOK) {
            leavePropagator();
        }
    }

    private void enterPropagator() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$spyMethodEnterPropagator);
    }

    private void leavePropagator() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$spyMethodLeavePropagator);
    }
}
