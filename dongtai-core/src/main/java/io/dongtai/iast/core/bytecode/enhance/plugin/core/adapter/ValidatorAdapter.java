package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.handler.hookpoint.models.policy.ValidatorNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Set;

public class ValidatorAdapter extends MethodAdapter {
    /**
     * @param adapter
     * @param mv
     * @param context
     * @param policyNodes
     */
    @Override
    public void onMethodEnter(MethodAdviceAdapter adapter, MethodVisitor mv, MethodContext context, Set<PolicyNode> policyNodes) {
    }

    /**
     * @param adapter
     * @param mv
     * @param opcode
     * @param context
     * @param policyNodes
     */
    @Override
    public void onMethodExit(MethodAdviceAdapter adapter, MethodVisitor mv, int opcode, MethodContext context, Set<PolicyNode> policyNodes) {
        for (PolicyNode policyNode : policyNodes) {
            if (!(policyNode instanceof ValidatorNode)) {
                continue;
            }

            Label elseLabel = new Label();
            Label endLabel = new Label();

            isEnterScope(adapter);
            mv.visitJumpInsn(Opcodes.IFEQ, elseLabel);

            adapter.trackMethod(opcode, policyNode, true);

            adapter.mark(elseLabel);
            adapter.mark(endLabel);
        }
    }

    private void isEnterScope(MethodAdviceAdapter adapter) {
        adapter.invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        adapter.invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterValidator);
    }
}
