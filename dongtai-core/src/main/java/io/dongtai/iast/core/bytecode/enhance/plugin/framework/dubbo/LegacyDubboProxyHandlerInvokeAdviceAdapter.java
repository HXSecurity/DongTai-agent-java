package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class LegacyDubboProxyHandlerInvokeAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method GET_ARGUMENTS_METHOD = Method.getMethod("java.lang.Object[] getArguments()");
    private static final Method GET_GETATTACHMENTS_METHOD = Method.getMethod("java.util.Map getAttachments()");
    private static final Method GET_METHOD_NAME_METHOD = Method.getMethod("java.lang.String getMethodName()");

    private final Type invocationType;

    protected LegacyDubboProxyHandlerInvokeAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature, ClassContext context) {
        super(mv, access, name, desc, context, "dubbo", signature);
        this.invocationType = Type.getObjectType(" com/alibaba/dubbo/rpc/Invocation".substring(1));
    }

    @Override
    protected void before() {
        mark(tryLabel);

        enterScope(Scope.DUBBO_SOURCE);

        Label elseLabel = new Label();
        isFirstLevelScope(Scope.DUBBO_SOURCE);
        mv.visitJumpInsn(EQ, elseLabel);
        collectDubboRequestSource();
        mark(elseLabel);
    }

    @Override
    protected void after(int opcode) {
        leaveScope(Scope.DUBBO_SOURCE);
    }

    private void collectDubboRequestSource() {
        Label tryL = new Label();
        Label catchL = new Label();
        Label exHandlerL = new Label();
        visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
        visitLabel(tryL);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThis();
        loadArg(0);
        loadArg(0);
        invokeInterface(this.invocationType, GET_METHOD_NAME_METHOD);
        loadArg(0);
        invokeInterface(this.invocationType, GET_ARGUMENTS_METHOD);
        loadArg(0);
        invokeInterface(this.invocationType, GET_GETATTACHMENTS_METHOD);
        push(this.classContext.getClassName());
        push(this.name);
        push(this.signature);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectDubboRequestSource);

        visitLabel(catchL);
        Label endL = new Label();
        visitJumpInsn(GOTO, endL);
        visitLabel(exHandlerL);
        visitVarInsn(ASTORE, this.nextLocal);
        visitLabel(endL);
    }
}
