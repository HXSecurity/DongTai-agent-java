package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SinkNode;
import org.objectweb.asm.*;

import java.util.Set;

public class SinkAdapter extends MethodAdapter {
    @Override
    public void onMethodEnter(MethodAdviceAdapter adapter, MethodVisitor mv, MethodContext context,
                              Set<PolicyNode> policyNodes) {
        for (PolicyNode policyNode : policyNodes) {
            if (!(policyNode instanceof SinkNode)) {
                continue;
            }
            if ("ssrf".equals(((SinkNode) policyNode).getVulType())){
                adapter.skipCollect(-1, policyNode, false);
            }

            enterScope(adapter, policyNode);

            Label elseLabel = new Label();
            Label endLabel = new Label();

            isFirstScope(adapter);
            mv.visitJumpInsn(Opcodes.IFEQ, elseLabel);

            adapter.trackMethod(-1, policyNode, false);

            adapter.mark(elseLabel);
            adapter.mark(endLabel);
        }
    }

    @Override
    public void onMethodExit(MethodAdviceAdapter adapter, MethodVisitor mv, int opcode, MethodContext context,
                             Set<PolicyNode> policyNodes) {
        for (PolicyNode policyNode : policyNodes) {
            if (!(policyNode instanceof SinkNode)) {
                continue;
            }

            leaveScope(adapter, policyNode);
        }
    }

    private void enterScope(MethodAdviceAdapter adapter, PolicyNode policyNode) {
        if (policyNode.isIgnoreInternal()) {
            adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterIgnoreInternal);
        }

        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterSink);
    }

    private void leaveScope(MethodAdviceAdapter adapter, PolicyNode policyNode) {
        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveSink);

        if (policyNode.isIgnoreInternal()) {
            adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveIgnoreInternal);
        }
    }

    private void isFirstScope(MethodAdviceAdapter adapter) {
        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelSink);
    }
}
