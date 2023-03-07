package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class DubboExchangeChannelSendAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method GET_RESULT_METHOD = Method.getMethod("java.lang.Object getResult()");
    private static final Method GET_STATUS_METHOD = Method.getMethod("byte getStatus()");

    private final String packageName;
    private final Type objectType;
    private final Type responseType;

    protected DubboExchangeChannelSendAdviceAdapter(MethodVisitor mv, int access, String name, String desc,
                                                    String signature, ClassContext context, String packageName) {
        super(mv, access, name, desc, context, "dubbo", signature);
        this.packageName = packageName;
        String packageDesc = packageName.replace(".", "/");
        this.responseType = Type.getObjectType(packageDesc + "/dubbo/remoting/exchange/Response");
        this.objectType = Type.getObjectType("java/lang/Object");
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();

        isFirstLevelDubbo();
        mv.visitJumpInsn(EQ, elseLabel);

        collectDubboResponse();

        mark(elseLabel);
    }

    @Override
    protected void after(int opcode) {
    }

    private void isFirstLevelDubbo() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelDubbo);
    }

    private void collectDubboResponse() {
        Label tryL = new Label();
        Label catchL = new Label();
        Label exHandlerL = new Label();
        visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
        visitLabel(tryL);


        int respLocal = newLocal(this.responseType);
        loadArg(0);
        checkCast(this.responseType);
        storeLocal(respLocal);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadLocal(respLocal);
        invokeVirtual(this.responseType, GET_RESULT_METHOD);
        loadLocal(respLocal);
        invokeVirtual(this.responseType, GET_STATUS_METHOD);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectDubboResponse);

        visitLabel(catchL);
        Label endL = new Label();
        visitJumpInsn(GOTO, endL);
        visitLabel(exHandlerL);
        visitVarInsn(ASTORE, this.nextLocal);
        visitLabel(endL);
    }
}
