package com.secnium.iast.core.enhance.plugins.api.spring;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class SpringApplicationAdviceAdapter extends AbstractAdviceAdapter {

    public SpringApplicationAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String type, String signCode) {
        super(mv, access, name, desc, context, type, signCode);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        mark(catchLabel);
    }

    @Override
    protected void after(int opcode) {
        if (!isThrow(opcode)) {
            Label endLabel = new Label();
            captureMethodState(opcode, HookType.SPRINGAPPLICATION.getValue(), true);
            mark(endLabel);
        } else {
            captureMethodState(opcode, HookType.SPRINGAPPLICATION.getValue(), true);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
    }
}
