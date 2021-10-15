package com.secnium.iast.core.enhance.plugins.framework.j2ee.dispatch;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletDispatcherAdviceAdapter extends AbstractAdviceAdapter {
    boolean isJakarta;

    public ServletDispatcherAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature, IastContext context, boolean isJakarta) {
        super(mv, access, name, desc, context, "j2ee", signature);
        this.isJakarta = isJakarta;
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();

        enterHttp();
        isFirstLevelHttp();
        mv.visitJumpInsn(EQ, elseLabel);

        cloneHttpServletRequest();
        cloneHttpServletResponse();
        captureMethodState(-1, HookType.HTTP.getValue(), false);
        mark(elseLabel);
    }

    /**
     * 离开HTTP方法时，将当前线程中的数据统一发送至云端，清空当前threadlocal的缓存数据，避免导致内存泄漏
     *
     * @param opcode
     */
    @Override
    protected void after(final int opcode) {
        leaveHttp();
    }

    private void enterHttp() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterHttp);
    }

    private void leaveHttp() {
        push(context.getNamespace());
        loadArg(1);
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveHttp);
    }

    private void isFirstLevelHttp() {
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelHttp);
    }

    /**
     * 克隆Http请求中的HttpServletRequest对象，但是，实际使用中，遇到request对象为多层封装的结果，无法转换为基类：HttpServletRequest，故，此方法弃用
     */
    protected void cloneHttpServletRequest() {
        push(context.getNamespace());
        loadArg(0);
        push(isJakarta);
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$cloneRequest);
        storeArg(0);
    }

    /**
     * 克隆Http请求中的HttpServletResponse对象，但是，实际使用中，遇到response对象为多层封装的结果，无法转换为基类：HttpServletRequest，故，此方法弃用
     * DongTai-IAST-Agent
     */
    protected void cloneHttpServletResponse() {
        push(context.getNamespace());
        loadArg(1);
        push(isJakarta);
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$cloneResponse);
        storeArg(1);
    }

}
