package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ServletOutputStreamWriteAdviceAdapter extends AbstractAdviceAdapter {
    public ServletOutputStreamWriteAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature,
                                                 ClassContext context) {
        super(mv, access, name, desc, context, "j2ee", signature);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterScope(Scope.SERVLET_OUTPUT_STREAM_WRITE);

        Label elseLabel = new Label();

        isFirstLevelScope(Scope.SERVLET_OUTPUT_STREAM_WRITE);
        mv.visitJumpInsn(EQ, elseLabel);

        onServletOutputStreamWrite();

        mark(elseLabel);
    }

    @Override
    protected void after(final int opcode) {
        leaveScope(Scope.SERVLET_OUTPUT_STREAM_WRITE);
    }

    private void onServletOutputStreamWrite() {
        if ("(I)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            loadArg(0);
            pushNull();
            push(-1);
            push(-1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletOutputStreamWrite);
        } else if ("([B)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            push(-1);
            loadArg(0);
            push(-1);
            push(-1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletOutputStreamWrite);
        } else if ("([BII)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            push(-1);
            loadArg(0);
            loadArg(1);
            loadArg(2);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onServletOutputStreamWrite);
        }
    }
}
