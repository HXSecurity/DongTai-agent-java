package io.dongtai.iast.core.bytecode.enhance.plugin.framework.krpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;

public class KrpcAPIAdviceAdapter extends AbstractAdviceAdapter{

    public KrpcAPIAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signCode, IastContext context) {
        super(mv, access, name, desc, context, "krpc_api", signCode);
    }

    @Override
    protected void onMethodEnter() {
        Label elseLabel = new Label();
        captureMethodState(-1, HookType.RPC.getValue(), false);
        mark(elseLabel);
    }

    @Override
    protected void onMethodExit(int opcode) {
    }

    @Override
    protected void before() {

    }

    @Override
    protected void after(int opcode) {

    }

    /**
     * 方法结束前，如何判断是否需要throw、return，解决堆栈未对齐
     *
     * @param maxStack
     * @param maxLocals
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mv.visitMaxs(maxStack, maxLocals);
    }
}
