package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class LegacyDubboExchangeHandleRequestAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method GET_URL_METHOD = Method.getMethod(" com.alibaba.dubbo.common.URL getUrl()".substring(1));
    private static final Method URL_TO_STRING_METHOD = Method.getMethod("java.lang.String toString()");
    private static final Method GET_REMOTE_ADDRESS_METHOD = Method.getMethod("java.net.InetSocketAddress getRemoteAddress()");
    private static final Method IS_TWO_WAY_METHOD = Method.getMethod("boolean isTwoWay()");
    private static final Method IS_EVENT_METHOD = Method.getMethod("boolean isEvent()");
    private static final Method IS_BROKEN_METHOD = Method.getMethod("boolean isBroken()");
    private static final Method IS_HEARTBEAT_METHOD = Method.getMethod("boolean isHeartbeat()");
    private static final Method GET_RESULT_METHOD = Method.getMethod("java.lang.Object getResult()");
    private static final Method GET_STATUS_METHOD = Method.getMethod("byte getStatus()");

    private final Type endpointType;
    private final Type urlType;
    private final Type channelType;
    private final Type requestType;
    private final Type responseType;

    protected LegacyDubboExchangeHandleRequestAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature, ClassContext context) {
        super(mv, access, name, desc, context, "dubbo", signature);
        this.endpointType = Type.getObjectType(" com/alibaba/dubbo/remoting/Endpoint".substring(1));
        this.urlType = Type.getObjectType(" com/alibaba/dubbo/common/URL".substring(1));
        this.channelType = Type.getObjectType(" com/alibaba/dubbo/remoting/Channel".substring(1));
        this.requestType = Type.getObjectType(" com/alibaba/dubbo/remoting/exchange/Request".substring(1));
        this.responseType = Type.getObjectType(" com/alibaba/dubbo/remoting/exchange/Response".substring(1));
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
        // if (opcode != ATHROW) {
        //     Label elseLabel = new Label();
        //     isFirstLevelDubbo();
        //     mv.visitJumpInsn(EQ, elseLabel);
        //     collectDubboResponse(opcode);
        //     mark(elseLabel);
        // }

        leaveDubbo(opcode);
    }

    private void enterDubbo() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterDubbo);
    }

    private void leaveDubbo(int opcode) {
        int retLocal = newLocal(ASM_TYPE_OBJECT);
        if (!isThrow(opcode)) {
            loadReturn(opcode);
        } else {
            pushNull();
        }
        storeLocal(retLocal);
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(1);
        loadLocal(retLocal);
        if (!isThrow(opcode)) {
            loadLocal(retLocal);
            invokeVirtual(this.responseType, GET_RESULT_METHOD);
            loadLocal(retLocal);
            invokeVirtual(this.responseType, GET_STATUS_METHOD);
        } else {
            pushNull();
            byte b = 0;
            push(b);
        }
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
        invokeInterface(this.endpointType, GET_URL_METHOD);
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
        visitVarInsn(ASTORE, this.nextLocal);
        visitLabel(endL);
    }
}
