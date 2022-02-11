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
public class SinkAdviceAdapter extends AbstractAdviceAdapter {

    private static final boolean ENABLE_ALL_HOOK = PropertyUtils.getInstance().isEnableAllHook();

    public SinkAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context,
            String framework, String signCode, boolean overpower) {
        super(mv, access, name, desc, context, framework, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();
        if (!ENABLE_ALL_HOOK) {
            enterSink();
            isTopLevelSink();
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
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterSink);
    }

    /**
     * 判断是否位于顶级sink方法的字节码
     */
    private void isTopLevelSink() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelSink);
    }

    /**
     * 离开sink方法的字节码
     */
    private void leaveSink() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveSink);
    }
}
