package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ServletInputStreamReadAdviceAdapter extends AbstractAdviceAdapter {
    public ServletInputStreamReadAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature,
                                               ClassContext context) {
        super(mv, access, name, desc, context, "j2ee", signature);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterScope(Scope.SERVLET_INPUT_STREAM_READ);
    }

    @Override
    protected void after(final int opcode) {
        if (opcode != ATHROW) {
            Label elseLabel = new Label();

            isFirstLevelScope(Scope.SERVLET_INPUT_STREAM_READ);
            mv.visitJumpInsn(EQ, elseLabel);

            onServletInputStreamRead(opcode);

            mark(elseLabel);
        }

        leaveScope(Scope.SERVLET_INPUT_STREAM_READ);
    }

    private void onServletInputStreamRead(int opcode) {
        if ("()I".equals(this.desc)) {
            dup();
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            swap();
            push(desc);
            loadThis();
            pushNull();
            push(-1);
            push(-1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletInputStreamRead);
        } else if ("([B)I".equals(this.desc)) {
            dup();
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            swap();
            push(desc);
            loadThis();
            loadArg(0);
            push(-1);
            push(-1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletInputStreamRead);
        } else if ("([BII)I".equals(this.desc)) {
            dup();
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            swap();
            push(desc);
            loadThis();
            loadArg(0);
            loadArg(1);
            loadArg(2);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletInputStreamRead);
        }
    }
}
