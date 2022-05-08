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
/*        Label elseLabel2 = new Label();
        isRequestReplay();
        mv.visitJumpInsn(EQ, elseLabel2);
        Label returnLabel = new Label();
        mv.visitTypeInsn(NEW, "java/lang/NullPointerException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("DongTai agent request replay, please ignore");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mark(returnLabel);*/
/*        Label label1 = new Label();
        mv.visitTryCatchBlock(tryLabel, label1, label1, "java/lang/RuntimeException");
        mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("asaaaaa");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
        mv.visitLabel(label1);
        mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/RuntimeException"});
        mv.visitVarInsn(ASTORE, 1);
        Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitLdcInsn("DongTai agent request replay, please ignore");
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("DongTai agent request replay, please ignore");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);*/
//        mark(elseLabel2);
        mark(elseLabel);
    }

    @Override
    protected void after(final int opcode) {
        if (!ENABLE_ALL_HOOK) {
            leaveSink();
        }
    }

/*    *//**
     * 方法结束前，如何判断是否需要throw、return，解决堆栈未对齐
     *
     * @param maxStack
     * @param maxLocals
     *//*
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mark(catchLabel);
        visitTryCatchBlock(tryLabel, catchLabel, mark(), ASM_TYPE_THROWABLE.getInternalName());
        Label elseLabel2 = new Label();
        isNotRequestReplay();
        mv.visitJumpInsn(EQ, elseLabel2);
        Label returnLabel = new Label();
        throwException();
        mark(returnLabel);
        mark(elseLabel2);
        after(ATHROW);
        if (mv != null) {
            mv.visitMaxs(maxStack, maxLocals);
        }
    }*/

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

    private void isNotRequestReplay() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isNotReplayRequest);
    }

    private void isRequestReplay() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isReplayRequest);
    }

    /**
     * 离开sink方法的字节码
     */
    private void leaveSink() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveSink);
    }
}
