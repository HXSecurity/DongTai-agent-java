package io.dongtai.iast.core.bytecode.enhance.plugin.framework.feign;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;

public class FeignSyncHandlerInvokeAdviceAdapter extends AbstractAdviceAdapter {
    private Label exHandler;

    protected FeignSyncHandlerInvokeAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature, ClassContext context) {
        super(mv, access, name, desc, context, "feign", signature);
    }

    @Override
    protected void onMethodEnter() {
        this.tryLabel = new Label();
        visitLabel(this.tryLabel);
        enterMethod();
        this.catchLabel = new Label();
        this.exHandler = new Label();
    }

    @Override
    protected void onMethodExit(int opcode) {
        leaveMethod(opcode);
    }

    @Override
    protected void before() {
    }

    @Override
    protected void after(int opcode) {
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitLabel(this.catchLabel);
        visitLabel(this.exHandler);
        leaveMethod(ATHROW);
        throwException();
        visitTryCatchBlock(this.tryLabel, this.catchLabel, this.exHandler, ASM_TYPE_THROWABLE.getInternalName());
        super.visitMaxsNew(maxStack, maxLocals);
    }

    private void enterMethod() {
        skipCollect();
        enterScope();

        Label elseLabel = new Label();
        Label endLabel = new Label();

        isFirstScope();
        mv.visitJumpInsn(Opcodes.IFEQ, elseLabel);

        traceMethod();

        mark(elseLabel);
        mark(endLabel);
    }

    private void leaveMethod(int opcode) {
        leaveScope();
    }

    private void enterScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(false);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterPropagator);
    }

    private void leaveScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(false);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leavePropagator);
    }

    private void isFirstScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelPropagator);
    }

    private void traceMethod() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThisOrPushNullIfIsStatic();
        loadArgArray();
        push(this.classContext.getClassName());
        push(this.name);
        push(this.signature);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$traceFeignInvoke);
        pop();
    }

    private void skipCollect() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThisOrPushNullIfIsStatic();
        invokeInterface(ASM_TYPE_SPY_DISPATCHER,SPY$isSkipCollectFeign);
        pop();
    }
}
