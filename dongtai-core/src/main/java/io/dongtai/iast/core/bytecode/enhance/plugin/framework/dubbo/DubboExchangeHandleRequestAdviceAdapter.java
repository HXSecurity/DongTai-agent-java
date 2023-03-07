package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class DubboExchangeHandleRequestAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method URL_TO_STRING_METHOD = Method.getMethod("java.lang.String toString()");
    private static final Method GET_REMOTE_ADDRESS_METHOD = Method.getMethod("java.net.InetSocketAddress getRemoteAddress()");
    private static final Method IS_TWO_WAY_METHOD = Method.getMethod("boolean isTwoWay()");
    private static final Method IS_EVENT_METHOD = Method.getMethod("boolean isEvent()");
    private static final Method IS_BROKEN_METHOD = Method.getMethod("boolean isBroken()");
    private static final Method IS_HEARTBEAT_METHOD = Method.getMethod("boolean isHeartbeat()");
    private static final Method GET_RESULT_METHOD = Method.getMethod("java.lang.Object getResult()");
    private static final Method GET_STATUS_METHOD = Method.getMethod("byte getStatus()");

    private final String packageName;
    private final Type endpointType;
    private final Type urlType;
    private final Type channelType;
    private final Type requestType;
    private final Type responseType;
    private final Method getUrlMethod;

    protected DubboExchangeHandleRequestAdviceAdapter(MethodVisitor mv, int access, String name, String desc,
                                                      String signature, ClassContext context, String packageName) {
        super(mv, access, name, desc, context, "dubbo", signature);
        this.packageName = packageName;
        String packageDesc = packageName.replace(".", "/");
        this.endpointType = Type.getObjectType(packageDesc + "/dubbo/remoting/Endpoint");
        this.urlType = Type.getObjectType(packageDesc + "/dubbo/common/URL");
        this.channelType = Type.getObjectType(packageDesc + "/dubbo/remoting/Channel");
        this.requestType = Type.getObjectType(packageDesc + "/dubbo/remoting/exchange/Request");
        if (" com.alibaba".substring(1).equals(packageName)) {
            this.responseType = Type.getObjectType(packageDesc + "/dubbo/remoting/exchange/Response");
        } else {
            // org.apache.dubbo use HeaderExchangeChannel to track response
            this.responseType = null;
        }

        this.getUrlMethod = Method.getMethod(packageName + ".dubbo.common.URL getUrl()");
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();

        enterDubbo();
        isFirstLevelDubbo();
        mv.visitJumpInsn(EQ, elseLabel);

        collectDubboRequest();

        mark(elseLabel);
    }

    @Override
    protected void after(int opcode) {
        if (this.responseType != null && opcode != ATHROW) {
            Label elseLabel = new Label();
            isFirstLevelDubbo();
            mv.visitJumpInsn(EQ, elseLabel);
            collectDubboResponse(opcode);
            mark(elseLabel);
        }

        leaveDubbo(opcode);
    }

    private void enterDubbo() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterDubbo);
    }

    private void leaveDubbo(int opcode) {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        loadArg(1);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveDubbo);
    }

    private void isFirstLevelDubbo() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelDubbo);
    }

    private void collectDubboRequest() {
        Label tryL = new Label();
        Label catchL = new Label();
        Label exHandlerL = new Label();
        visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
        visitLabel(tryL);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThis();
        loadArg(0);
        loadArg(1);
        loadArg(0);
        invokeInterface(this.endpointType, this.getUrlMethod);
        invokeVirtual(this.urlType, URL_TO_STRING_METHOD);
        loadArg(0);
        invokeInterface(this.channelType, GET_REMOTE_ADDRESS_METHOD);
        loadArg(1);
        invokeVirtual(this.requestType, IS_TWO_WAY_METHOD);
        loadArg(1);
        invokeVirtual(this.requestType, IS_EVENT_METHOD);
        loadArg(1);
        invokeVirtual(this.requestType, IS_BROKEN_METHOD);
        loadArg(1);
        invokeVirtual(this.requestType, IS_HEARTBEAT_METHOD);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectDubboRequest);

        visitLabel(catchL);
        Label endL = new Label();
        visitJumpInsn(GOTO, endL);
        visitLabel(exHandlerL);
        int nextL = newLocal(ASM_TYPE_OBJECT);
        storeLocal(nextL);
        visitLabel(endL);
    }

    private void collectDubboResponse(int opcode) {
        int retLocal = newLocal(ASM_TYPE_OBJECT);
        loadReturn(opcode);
        storeLocal(retLocal);

        Label nonNullLabel = new Label();
        loadLocal(retLocal);
        ifNull(nonNullLabel);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadLocal(retLocal);
        invokeVirtual(this.responseType, GET_RESULT_METHOD);
        loadLocal(retLocal);
        invokeVirtual(this.responseType, GET_STATUS_METHOD);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectDubboResponse);

        mark(nonNullLabel);
    }
}
