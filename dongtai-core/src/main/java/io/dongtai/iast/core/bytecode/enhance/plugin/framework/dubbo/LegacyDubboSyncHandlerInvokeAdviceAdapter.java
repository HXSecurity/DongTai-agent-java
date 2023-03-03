package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class LegacyDubboSyncHandlerInvokeAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method GET_URL_METHOD = Method.getMethod(" com.alibaba.dubbo.common.URL getUrl()".substring(1));
    private static final Method GET_ARGUMENTS_METHOD = Method.getMethod("java.lang.Object[] getArguments()");
    private static final Method GET_GETATTACHMENTS_METHOD = Method.getMethod("java.util.Map getAttachments()");
    private static final Method URL_TO_STRING_METHOD = Method.getMethod("java.lang.String toString()");

    private Label exHandler;
    private final Type handlerType;
    private final Type urlType;
    private final Type invocationType;

    protected LegacyDubboSyncHandlerInvokeAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature, ClassContext context) {
        super(mv, access, name, desc, context, "dubbo", signature);
        this.handlerType = Type.getObjectType(" com/alibaba/dubbo/rpc/listener/ListenerInvokerWrapper".substring(1));
        this.invocationType = Type.getObjectType(" com/alibaba/dubbo/rpc/Invocation".substring(1));
        this.urlType = Type.getObjectType(" com/alibaba/dubbo/common/URL".substring(1));
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
        loadThis();
        dup();
        invokeVirtual(this.handlerType, GET_URL_METHOD);
        invokeVirtual(this.urlType, URL_TO_STRING_METHOD);
        loadArg(0);
        loadArg(0);
        invokeInterface(this.invocationType, GET_ARGUMENTS_METHOD);
        loadArg(0);
        invokeInterface(this.invocationType, GET_GETATTACHMENTS_METHOD);
        push(this.classContext.getClassName());
        push(this.name);
        push(this.signature);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$traceDubboInvoke);
        pop();
    }
}
