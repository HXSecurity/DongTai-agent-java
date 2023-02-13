package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletDispatcherAdviceAdapter extends AbstractAdviceAdapter {
    private static final Method GET_REQUEST_URL_METHOD = Method.getMethod("java.lang.StringBuffer getRequestURL()");
    private static final Method GET_REQUEST_URI_METHOD = Method.getMethod("java.lang.String getRequestURI()");
    private static final Method GET_QUERY_STRING_METHOD = Method.getMethod("java.lang.String getQueryString()");
    private static final Method GET_METHOD_METHOD = Method.getMethod("java.lang.String getMethod()");
    private static final Method GET_PROTOCOL_METHOD = Method.getMethod("java.lang.String getProtocol()");
    private static final Method GET_SCHEME_METHOD = Method.getMethod("java.lang.String getScheme()");
    private static final Method GET_SERVER_NAME_METHOD = Method.getMethod("java.lang.String getServerName()");
    private static final Method GET_CONTEXT_PATH_METHOD = Method.getMethod("java.lang.String getContextPath()");
    private static final Method GET_SERVLET_PATH_METHOD = Method.getMethod("java.lang.String getServletPath()");
    private static final Method GET_REMOTE_ADDR_METHOD = Method.getMethod("java.lang.String getRemoteAddr()");
    private static final Method IS_SECURE_METHOD = Method.getMethod("boolean isSecure()");
    private static final Method GET_SERVER_PORT_METHOD = Method.getMethod("int getServerPort()");
    private static final Method GET_HEADER_NAMES_METHOD = Method.getMethod("java.util.Enumeration getHeaderNames()");
    private static final Method GET_RESPONSE_HEADER_NAMES_METHOD = Method.getMethod("java.util.Collection getHeaderNames()");
    private static final Method GET_STATUS_METHOD = Method.getMethod("int getStatus()");
    private static final String GET_INPUT_STREAM_METHOD = ".servlet.ServletInputStream getInputStream()";

    private final String packageName;
    private final Type servletRequestType;
    private final Type servletResponseType;
    private final int reqIndex;
    private final int respIndex;

    public ServletDispatcherAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature,
                                          ClassContext context, String packageName) {
        super(mv, access, name, desc, context, "j2ee", signature);
        this.packageName = packageName;
        this.servletRequestType = Type.getObjectType(packageName + "/servlet/http/HttpServletRequest");
        this.servletResponseType = Type.getObjectType(packageName + "/servlet/http/HttpServletResponse");
        this.reqIndex = 0;
        this.respIndex = 1;
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();

        enterHttp();
        isFirstLevelHttp();
        mv.visitJumpInsn(EQ, elseLabel);

        collectHttpRequest();

        mark(elseLabel);
    }

    /**
     * 离开HTTP方法时，将当前线程中的数据统一发送至云端，清空当前threadlocal的缓存数据，避免导致内存泄漏
     *
     * @param opcode
     */
    @Override
    protected void after(final int opcode) {
        if (opcode != ATHROW) {
            Label elseLabel = new Label();
            isFirstLevelHttp();
            mv.visitJumpInsn(EQ, elseLabel);
            collectHttpResponse(opcode);
            mark(elseLabel);
        }

        leaveHttp();
    }

    private void enterHttp() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterHttp);
    }

    private void leaveHttp() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        loadArg(1);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveHttp);
    }

    private void isFirstLevelHttp() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelHttp);
    }

    private void collectHttpRequest() {
        Label tryL = new Label();
        Label catchL = new Label();
        Label exHandlerL = new Label();
        visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
        visitLabel(tryL);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThis();
        loadArg(this.reqIndex);
        loadArg(this.respIndex);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_REQUEST_URL_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_REQUEST_URI_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_QUERY_STRING_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_METHOD_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_PROTOCOL_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_SCHEME_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_SERVER_NAME_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_CONTEXT_PATH_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_REMOTE_ADDR_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, IS_SECURE_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_SERVER_PORT_METHOD);
        loadArg(this.reqIndex);
        invokeInterface(this.servletRequestType, GET_HEADER_NAMES_METHOD);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectHttpRequest);

        visitLabel(catchL);
        Label endL = new Label();
        visitJumpInsn(GOTO, endL);
        visitLabel(exHandlerL);
        visitVarInsn(ASTORE, nextLocal);
        visitLabel(endL);
    }

    private void collectHttpResponse(final int opcode) {
        Label tryL = new Label();
        Label catchL = new Label();
        Label exHandlerL = new Label();
        visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
        visitLabel(tryL);

        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThis();
        loadArg(this.reqIndex);
        loadArg(this.respIndex);
        loadArg(this.respIndex);
        invokeInterface(this.servletResponseType, GET_RESPONSE_HEADER_NAMES_METHOD);
        loadArg(this.respIndex);
        invokeInterface(this.servletResponseType, GET_STATUS_METHOD);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectHttpResponse);

        visitLabel(catchL);
        Label endL = new Label();
        visitJumpInsn(GOTO, endL);
        visitLabel(exHandlerL);
        visitVarInsn(ASTORE, nextLocal);
        visitLabel(endL);
    }
}
