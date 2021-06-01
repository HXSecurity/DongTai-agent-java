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
public class SinkAdviceAdapter extends AbstractAdviceAdapter {
    private static final boolean ENABLE_ALL_HOOK = PropertyUtils.getInstance().isEnableAllHook();
    private boolean overpower;

    public SinkAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String framework, String signCode, boolean overpower) {
        super(mv, access, name, desc, context, framework, signCode);
        this.overpower = overpower;
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();
        if (!ENABLE_ALL_HOOK) {
            enterSink();
            // 根据参数判断使用顶级sink还是sink
            if (overpower) {
                hasTaint();
            } else {
                isTopLevelSink();
            }
            mv.visitJumpInsn(EQ, elseLabel);
        }
        captureMethodState(-1, HookType.SINK.getValue(), false);
        mark(elseLabel);
    }

    @Override
    protected void after(final int opcode) {
        if (!ENABLE_ALL_HOOK) {
            leaveSink();
        }
    }

    /**
     * 进入sink方法的字节码
     */
    private void enterSink() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterSink);
    }

    /**
     * 判断是否位于顶级sink方法的字节码
     */
    private void isTopLevelSink() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelSink);
    }

    /**
     * 判断是否进入http且具有污点 isTopLevelSink()
     */
    private void hasTaint() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$hasTaint);
    }

    /**
     * 离开sink方法的字节码
     */
    private void leaveSink() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveSink);
    }
}
