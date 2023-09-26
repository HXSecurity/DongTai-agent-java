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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Set<String> matchedClassNameSet = policy.getMatchedClass(classContext, className, ancestors);

        // 匹配的时候增加日志方便根据类或者策略观测定位问题
        if (0 == matchedClassNameSet.size()) {
            DongTaiLog.trace("class = {}, no matching policy, so ignored.", classContext.getClassName());
            return classVisitor;
        }
        DongTaiLog.trace("class = {}, matching policy classes = {}", classContext.getClassName(), String.join(", ", matchedClassNameSet));

        classContext.setMatchedClassSet(matchedClassNameSet);
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
                    new ValidatorAdapter(),
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

            String matchedSignature;
            boolean methodIsTransformed = false;
            for (String matchedName : context.getMatchedClassSet()) {
                context.setMatchedClassName(matchedName);
                matchedSignature = AsmUtils.buildSignature(matchedName, name, descriptor);
                mv = lazyAop(mv, access, name, descriptor, matchedSignature, methodContext);
                methodIsTransformed = mv instanceof MethodAdviceAdapter;
                if (methodIsTransformed) break;

            }
            if (methodIsTransformed && this.classVersion <= Opcodes.V1_6) {
                mv = new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);
            }

            if (methodIsTransformed) {
                DongTaiLog.trace("rewrite method {} for listener[class={}]", context.getMatchedClassName(), context.getClassName());
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
