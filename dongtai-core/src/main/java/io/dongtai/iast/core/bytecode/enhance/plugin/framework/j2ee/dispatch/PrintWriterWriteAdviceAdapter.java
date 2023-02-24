package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PrintWriterWriteAdviceAdapter extends AbstractAdviceAdapter {
    public PrintWriterWriteAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signature,
                                         ClassContext context) {
        super(mv, access, name, desc, context, "j2ee", signature);
    }

    @Override
    protected void before() {
        mark(tryLabel);
        enterScope(Scope.SERVLET_OUTPUT_WRITE);

        Label elseLabel = new Label();

        isFirstLevelScope(Scope.SERVLET_OUTPUT_WRITE);
        mv.visitJumpInsn(EQ, elseLabel);

        onPrintWriterWrite();

        mark(elseLabel);
    }

    @Override
    protected void after(final int opcode) {
        leaveScope(Scope.SERVLET_OUTPUT_WRITE);
    }

    private void onPrintWriterWrite() {
        if ("(I)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            loadArg(0);
            pushNull();
            pushNull();
            push(-1);
            push(-1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onPrintWriterWrite);
        } else if ("([CII)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            push(-1);
            pushNull();
            loadArg(0);
            loadArg(1);
            loadArg(2);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onPrintWriterWrite);
        } else if ("(Ljava/lang/String;II)V".equals(this.desc)) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(desc);
            loadThis();
            push(-1);
            loadArg(0);
            pushNull();
            loadArg(1);
            loadArg(2);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$onPrintWriterWrite);
        }
    }
}
