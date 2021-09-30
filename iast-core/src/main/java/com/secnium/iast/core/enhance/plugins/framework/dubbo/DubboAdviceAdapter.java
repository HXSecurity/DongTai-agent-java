package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DubboAdviceAdapter extends AbstractAdviceAdapter {

    public DubboAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signCode, IastContext context) {
        super(mv, access, name, desc, context, "dubbo", signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        Label elseLabel = new Label();
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$enterHttp);

        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$isFirstLevelHttp);
        mv.visitJumpInsn(EQ, elseLabel);

        captureMethodState(-1, HookType.DUBBO.getValue(), false);

        // 标记进入source点
        mark(elseLabel);
    }

    @Override
    protected void after(int opcode) {
        if (!isThrow(opcode)) {
            loadReturn(opcode);
        }
        push(context.getNamespace());
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$leaveHttp);
    }
}
