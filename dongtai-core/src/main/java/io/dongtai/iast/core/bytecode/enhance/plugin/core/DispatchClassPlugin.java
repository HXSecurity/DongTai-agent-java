package io.dongtai.iast.core.bytecode.enhance.plugin.core;

import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.PropagateAdviceAdapter;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.SinkAdviceAdapter;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.SourceAdviceAdapter;
import io.dongtai.iast.core.handler.hookpoint.controller.HookType;
import io.dongtai.iast.core.handler.hookpoint.models.IastHookRuleModel;
import io.dongtai.iast.core.handler.hookpoint.models.IastSinkModel;
import io.dongtai.iast.core.handler.hookpoint.vulscan.VulnType;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.iast.core.utils.matcher.Method;
import io.dongtai.log.DongTaiLog;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchClassPlugin implements DispatchPlugin {

    private final boolean enableAllHook;
    private Set<String> ancestors;
    private String className;

    public DispatchClassPlugin() {
        this.enableAllHook = EngineManager.getInstance().supportLazyHook();
    }

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ClassVisit modifiedClassVisitor = null;
        ancestors = context.getAncestors();
        className = context.getClassName();
        String matchClassName = isMatch();

        if (null != matchClassName) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("class {} hit rule {}, class diagrams: {}", className, matchClassName,
                        Arrays.toString(ancestors.toArray()));
            }
            context.setMatchClassName(matchClassName);
            modifiedClassVisitor = new ClassVisit(classVisitor, context);
        } else if (enableAllHook && !context.isBootstrapClassLoader()) {
            context.setMatchClassName(className);
            modifiedClassVisitor = new ClassVisit(classVisitor, context);
        }

        return modifiedClassVisitor == null ? classVisitor : modifiedClassVisitor;
    }

    @Override
    public String isMatch() {
        if (IastHookRuleModel.hookByName(className)) {
            return className;
        }

        for (String superClassName : ancestors) {
            if (IastHookRuleModel.hookBySuperClass(superClassName)) {
                return superClassName;
            }
        }
        return null;
    }

    public class ClassVisit extends AbstractClassVisitor {

        private final boolean isAppClass;
        private int classVersion;

        ClassVisit(ClassVisitor classVisitor, IastContext context) {
            super(classVisitor, context);
            this.isAppClass = false;
        }

        @Override
        public boolean hasTransformed() {
            return transformed;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if (!Modifier.isInterface(access) && !Modifier.isAbstract(access) && !"<clinit>".equals(name)) {
                String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassName(), name, desc);
                String framework = IastHookRuleModel.getFrameworkByMethodSignature(iastMethodSignature);

                mv = context.isEnableAllHook() ? greedyAop(mv, access, name, desc,
                        framework == null ? "none" : framework, iastMethodSignature)
                        : (framework == null ? mv : lazyAop(mv, access, name, desc, framework, iastMethodSignature));

                if (transformed && this.classVersion < 50) {
                    mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
                }

                if (transformed && DongTaiLog.isDebugEnabled() && null != framework) {
                    DongTaiLog.debug("rewrite method {} for listener[framework={},class={}]", iastMethodSignature,
                            framework, context.getClassName());
                }
            }
            return mv;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            this.classVersion = version;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        /**
         * 贪婪AOP，用于处理全量HOOK
         *
         * @param mv        方法访问器
         * @param access    方法访问控制符
         * @param name      方法名
         * @param desc      方法描述符
         * @param framework 框架名称
         * @param signature 方法签名
         * @return 修改后的方法访问器
         */
        private MethodVisitor greedyAop(MethodVisitor mv, int access, String name, String desc, String framework,
                String signature) {
            if (null != framework) {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, signature);
            } else if (isAppClass && Method.hook(access, name, desc, signature)) {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, null, signature);
            }
            transformed = true;
            return mv;
        }

        /**
         * 懒惰AOP，用于处理预定义HOOK点
         *
         * @param mv        方法访问器
         * @param access    方法访问控制符
         * @param name      方法名
         * @param desc      方法描述符
         * @param framework 框架名称
         * @param signature 方法签名
         * @return 修改后的方法访问器
         */
        private MethodVisitor lazyAop(MethodVisitor mv, int access, String name, String desc, String framework,
                String signature) {
            int hookValue = IastHookRuleModel.getRuleTypeValueByFramework(framework);
            if (HookType.PROPAGATOR.equals(hookValue)) {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, signature);
                transformed = true;
            } else if (HookType.SINK.equals(hookValue)) {
                // fixme 针对越权类，overpower为true，否则为false
                IastSinkModel sinkModel = IastHookRuleModel.getSinkByMethodSignature(signature);
                if (sinkModel != null) {
                    boolean isOverPower = VulnType.SQL_OVER_POWER.equals(sinkModel.getType());
                    mv = new SinkAdviceAdapter(mv, access, name, desc, context, framework, signature, isOverPower);
                    transformed = true;
                } else {
                    DongTaiLog.error("framework[{}], method[{}] doesn't find sink model", framework, name);
                }
            } else if (HookType.SOURCE.equals(hookValue)) {
                mv = new SourceAdviceAdapter(mv, access, name, desc, context, framework, signature);
                transformed = true;
            }
            return mv;
        }
    }
}
