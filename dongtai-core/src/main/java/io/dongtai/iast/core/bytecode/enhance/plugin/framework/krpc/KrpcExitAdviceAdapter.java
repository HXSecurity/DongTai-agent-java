package io.dongtai.iast.core.bytecode.enhance.plugin.framework.krpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class KrpcExitAdviceAdapter extends AbstractAdviceAdapter{

    private int athrowCounts = 0;

    public KrpcExitAdviceAdapter(MethodVisitor mv, int access, String name, String desc, String signCode, IastContext context) {
        super(mv, access, name, desc, context, "krpc", signCode);
    }

    @Override
    protected void onMethodEnter() {
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == ATHROW) {
            if (athrowCounts == 0) {
                athrowCounts++;
                leaveKrpc();
            }
        } else {
            leaveKrpc();
        }
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

    /**
     * mark for leave Krpc method
     * <p>
     * since: 1.3.2
     */
    private void leaveKrpc() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveKrpc);
    }

}
