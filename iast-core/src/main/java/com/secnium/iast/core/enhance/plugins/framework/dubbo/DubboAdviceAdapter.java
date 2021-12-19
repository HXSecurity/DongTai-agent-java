package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DubboAdviceAdapter extends AbstractAdviceAdapter {

    public DubboAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signCode,
            IastContext context) {
        super(mv, access, name, desc, context, "dubbo", signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();

        enterDubbo();
        isFirstLevelDubbo();
        mv.visitJumpInsn(EQ, elseLabel);
        captureMethodState(-1, HookType.DUBBO.getValue(), false);
        mark(elseLabel);
    }

    @Override
    protected void after(int opcode) {
//        if (!isThrow(opcode)) {
//            loadReturn(opcode);
//        }
//        leaveDubbo();
    }

    /**
     * mark for enter dubbo method
     * <p>
     * since: 1.1.4
     */
    private void enterDubbo() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterDubbo);
    }

    /**
     * Determine whether it is the first layer of Dubbo method call
     * <p>
     * since: 1.1.4
     */
    private void isFirstLevelDubbo() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelDubbo);
    }

    /**
     * mark for leave dubbo method
     * <p>
     * since: 1.1.4
     */
    private void leaveDubbo() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveDubbo);
    }


}
