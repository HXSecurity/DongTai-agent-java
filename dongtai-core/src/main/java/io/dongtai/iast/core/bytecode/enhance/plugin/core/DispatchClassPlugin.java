package io.dongtai.iast.core.bytecode.enhance.plugin.core;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.*;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchClassPlugin implements DispatchPlugin {
    private Set<String> ancestors;
    private String className;

    public DispatchClassPlugin() {
    }

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext classContext, Policy policy) {
        ancestors = classContext.getAncestors();
        className = classContext.getClassName();
        String matchedClassName = policy.getMatchedClass(className, ancestors);

        if (null == matchedClassName) {
            return classVisitor;
        }

        classContext.setMatchedClassName(matchedClassName);
        return new ClassVisit(classVisitor, classContext, policy);
    }

    @Override
    public String getName() {
        return "class";
    }

    public class ClassVisit extends AbstractClassVisitor {
        private int classVersion;
        private final MethodAdapter[] methodAdapters;

        ClassVisit(ClassVisitor classVisitor, ClassContext classContext, Policy policy) {
            super(classVisitor, classContext, policy);
            this.methodAdapters = new MethodAdapter[]{
                    new SourceAdapter(),
                    new PropagatorAdapter(),
                    new SinkAdapter(),
            };
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                          String[] interfaces) {
            this.classVersion = version;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                         final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (Modifier.isInterface(access) || Modifier.isAbstract(access) || "<clinit>".equals(name)) {
                if (this.classVersion <= Opcodes.V1_6) {
                    mv = new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);
                }
                return mv;
            }

            if (this.policy.isBlacklistHooks(this.context.getClassName())
                    && !this.policy.isIgnoreBlacklistHooks(this.context.getClassName())
                    && !this.policy.isIgnoreInternalHooks(this.context.getClassName())) {
                if (this.classVersion <= Opcodes.V1_6) {
                    mv = new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);
                }
                return mv;
            }

            MethodContext methodContext = new MethodContext(this.context, name);
            methodContext.setModifier(access);
            methodContext.setDescriptor(descriptor);
            methodContext.setParameters(AsmUtils.buildParameterTypes(descriptor));

            String matchedSignature = AsmUtils.buildSignature(context.getMatchedClassName(), name, descriptor);

            mv = lazyAop(mv, access, name, descriptor, matchedSignature, methodContext);
            boolean methodIsTransformed = mv instanceof MethodAdviceAdapter;

            if (methodIsTransformed && this.classVersion <= Opcodes.V1_6) {
                mv = new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);
            }

            if (methodIsTransformed) {
                DongTaiLog.trace("rewrite method {} for listener[class={}]", matchedSignature, context.getClassName());
            }

            return mv;
        }

        /**
         * 懒惰AOP，用于处理预定义HOOK点
         *
         * @param mv         方法访问器
         * @param access     方法访问控制符
         * @param name       方法名
         * @param descriptor 方法描述符
         * @param signature  方法签名
         * @return 修改后的方法访问器
         */
        private MethodVisitor lazyAop(MethodVisitor mv, int access, String name, String descriptor, String signature,
                                      MethodContext methodContext) {
            Set<PolicyNode> matchedNodes = new HashSet<PolicyNode>();

            Map<String, PolicyNode> policyNodesMap = this.policy.getPolicyNodesMap();
            if (policyNodesMap != null && policyNodesMap.size() != 0) {
                for (Map.Entry<String, PolicyNode> entry : policyNodesMap.entrySet()) {
                    if (entry.getValue().getMethodMatcher().match(methodContext)) {
                        matchedNodes.add(entry.getValue());
                    }
                }
            }

            if (matchedNodes.size() > 0) {
                mv = new MethodAdviceAdapter(mv, access, name, descriptor, signature,
                        matchedNodes, methodContext, this.methodAdapters);
                setTransformed();
            }

            return mv;
        }
    }
}
