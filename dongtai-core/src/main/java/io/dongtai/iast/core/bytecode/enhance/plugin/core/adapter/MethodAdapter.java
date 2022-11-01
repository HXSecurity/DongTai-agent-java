package io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter;

import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

public abstract class MethodAdapter implements AsmTypes, AsmMethods {
    public abstract void onMethodEnter(MethodAdviceAdapter adapter, MethodVisitor mv, MethodContext context,
                                       Set<PolicyNode> policyNodes);
    public abstract void onMethodExit(MethodAdviceAdapter adapter, MethodVisitor mv, int opcode, MethodContext context,
                                      Set<PolicyNode> policyNodes);
}
