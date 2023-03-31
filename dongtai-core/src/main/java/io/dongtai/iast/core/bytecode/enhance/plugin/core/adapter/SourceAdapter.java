package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.SourceNode;
import org.objectweb.asm.*;

import java.util.Set;

public class SourceAdapter extends MethodAdapter {
    @Override
    public void onMethodEnter(MethodAdviceAdapter adapter, MethodVisitor mv, MethodContext context,
                              Set<PolicyNode> policyNodes) {
        for (PolicyNode policyNode : policyNodes) {
            if (!(policyNode instanceof SourceNode)) {
                continue;
            }

            enterScope(adapter, policyNode);
        }
    }

    @Override
    public void onMethodExit(MethodAdviceAdapter adapter, MethodVisitor mv, int opcode, MethodContext context,
                             Set<PolicyNode> policyNodes) {
        for (PolicyNode policyNode : policyNodes) {
            if (!(policyNode instanceof SourceNode)) {
                continue;
            }

            Label elseLabel = new Label();
            Label endLabel = new Label();

            isFirstScope(adapter);
            mv.visitJumpInsn(Opcodes.IFEQ, elseLabel);

            adapter.trackMethod(opcode, policyNode, true);

            adapter.mark(elseLabel);
            adapter.mark(endLabel);

            leaveScope(adapter, policyNode);
        }
    }

    private void enterScope(MethodAdviceAdapter adapter, PolicyNode policyNode) {
        if (policyNode.isIgnoreInternal()) {
            adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterIgnoreInternal);
        }

        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterSource);
    }

    private void leaveScope(MethodAdviceAdapter adapter, PolicyNode policyNode) {
        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveSource);

        if (policyNode.isIgnoreInternal()) {
            adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveIgnoreInternal);
        }
    }

    private void isFirstScope(MethodAdviceAdapter adapter) {
        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelSource);
    }
}
