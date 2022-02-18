package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.utils.PropertyUtils;
import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropagateAdviceAdapter extends AbstractAdviceAdapter {

    private static final boolean ENABLE_ALL_HOOK = PropertyUtils.getInstance().isEnableAllHook();

    public PropagateAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context,
            String framework, String signCode) {
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
                invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
                invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelPropagator);
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
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterPropagator);
    }

    private void leavePropagator() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leavePropagator);
    }
}
