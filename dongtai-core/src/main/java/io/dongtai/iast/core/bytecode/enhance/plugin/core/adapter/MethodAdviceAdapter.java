package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

public class MethodAdviceAdapter extends AbstractAdviceAdapter implements AsmTypes, AsmMethods {
    private final Set<PolicyNode> policyNodes;
    private final MethodAdapter[] methodAdapters;
    private Label exHandler;

    public MethodAdviceAdapter(MethodVisitor mv, int access, String name, String descriptor, String signature,
                               Set<PolicyNode> policyNodes, MethodContext context, MethodAdapter[] methodAdapters) {
        super(mv, access, name, descriptor, signature, context);
        this.policyNodes = policyNodes;
        this.methodAdapters = methodAdapters;
    }

    @Override
    protected void before() {
    }

    @Override
    protected void after(int opcode) {
    }

    @Override
    protected void onMethodEnter() {
        if (this.policyNodes != null && !this.policyNodes.isEmpty()) {
            this.tryLabel = new Label();
            visitLabel(this.tryLabel);
            enterMethod();
            this.catchLabel = new Label();
            this.exHandler = new Label();
        }
    }

    private void enterMethod() {
        for (MethodAdapter methodAdapter : this.methodAdapters) {
            methodAdapter.onMethodEnter(this, this.mv, this.context, this.policyNodes);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW && this.policyNodes != null && !this.policyNodes.isEmpty()) {
            leaveMethod(opcode);
        }
    }

    private void leaveMethod(int opcode) {
        for (MethodAdapter methodAdapter : this.methodAdapters) {
            methodAdapter.onMethodExit(this, this.mv, opcode, this.context, this.policyNodes);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        if (this.policyNodes != null && !this.policyNodes.isEmpty()) {
            visitLabel(this.catchLabel);
            visitLabel(this.exHandler);
            leaveMethod(ATHROW);
            throwException();
            visitTryCatchBlock(this.tryLabel, this.catchLabel, this.exHandler, ASM_TYPE_THROWABLE.getInternalName());
            super.visitMaxsNew(maxStack, maxLocals);
        }
    }
}
