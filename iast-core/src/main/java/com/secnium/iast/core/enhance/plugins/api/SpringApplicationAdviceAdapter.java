package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.core.adapter.PropagateAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.MethodVisitor;

public class SpringApplicationAdviceAdapter extends PropagateAdviceAdapter {

    public SpringApplicationAdviceAdapter(MethodVisitor mv, int access, String name, String desc, IastContext context, String type, String signCode) {
        super(mv, access, name, desc, context, type, signCode);
    }

    @Override
    protected void before() {
    }

    @Override
    protected void after(int opcode) {
        if (!isThrow(opcode)) {
            captureMethodState(opcode, HookType.PROPAGATOR.getValue(), true);
        }
    }
}
