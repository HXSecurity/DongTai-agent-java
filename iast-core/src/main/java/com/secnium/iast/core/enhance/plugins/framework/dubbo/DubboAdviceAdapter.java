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

    private int athrowCounts = 0;

    public DubboAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signCode,
            IastContext context) {
        super(mv, access, name, desc, context, "dubbo", signCode);
    }

    @Override
    protected void onMethodEnter() {
        Label elseLabel = new Label();

        enterDubbo();
        isFirstLevelDubbo();
        mv.visitJumpInsn(EQ, elseLabel);
        captureMethodState(-1, HookType.DUBBO.getValue(), false);
        mark(elseLabel);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == ATHROW) {
            if (athrowCounts == 0) {
                athrowCounts++;
                leaveDubbo();
            }
        } else {
            leaveDubbo();
        }
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after(int opcode) {

    }

    /**
     * 方法结束前，如何判断是否需要throw、return，解决堆栈未对齐
     *
     * @param maxStack
     * @param maxLocals
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitMaxs(maxStack, maxLocals);
    }

    /**
     * mark for enter dubbo method
     * <p>
     * since: 1.2.0
     */
    private void enterDubbo() {
        push("DongTai");
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterDubbo);
    }

    /**
     * Determine whether it is the first layer of Dubbo method call
     * <p>
     * since: 1.2.0
     */
    private void isFirstLevelDubbo() {
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelDubbo);
    }

    /**
     * mark for leave dubbo method
     * <p>
     * since: 1.2.0
     */
    private void leaveDubbo() {
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveDubbo);
    }


}
