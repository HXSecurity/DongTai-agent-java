package com.secnium.iast.core.enhance.plugins;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.PropertyUtils;
import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.propagator.PropagateAdviceAdapter;
import com.secnium.iast.core.enhance.plugins.sinks.SinkAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import com.secnium.iast.core.handler.models.IastHookRuleModel;
import com.secnium.iast.core.handler.models.IastSinkModel;
import com.secnium.iast.core.handler.vulscan.VulnType;
import com.secnium.iast.core.util.AsmUtils;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.SandboxStringUtils;
import com.secnium.iast.core.util.matcher.ConfigMatcher;
import com.secnium.iast.core.util.matcher.Method;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchClassPlugin implements DispatchPlugin {
    private final static EngineManager JVM_SANDBOX = EngineManager.getInstance();
    private final static PropertyUtils PROPERTIES_UTILS = JVM_SANDBOX.getCfg();
    private final Logger logger;
    private final boolean enableAllHook;
    private HashSet<String> ancestors;
    private String classname;

    public DispatchClassPlugin() {
        this.enableAllHook = PROPERTIES_UTILS.isEnableAllHook();
        this.logger = LogUtils.getLogger(getClass());
    }

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ClassVisit modifiedClassVisitor = null;
        ancestors = context.getAncestors();
        classname = context.getClassName();
        String matchClassname = isMatch();


        if (null != matchClassname) {
            if (logger.isDebugEnabled()) {
                logger.debug("当前类 {} 命中规则 {} 类族为 {}", classname, matchClassname, Arrays.toString(ancestors.toArray()));
            }
            context.setMatchClassname(matchClassname);
            modifiedClassVisitor = new ClassVisit(classVisitor, context);
        } else if (enableAllHook && context.getClassLoader() != null) {
            context.setMatchClassname(classname);
            modifiedClassVisitor = new ClassVisit(classVisitor, context);
        }


        return modifiedClassVisitor == null ? classVisitor : modifiedClassVisitor;
    }

    @Override
    public String isMatch() {
        String javaClassname = SandboxStringUtils.toJavaClassName(classname);
        if (IastHookRuleModel.classIsNeededHookByName(javaClassname)) {
            return javaClassname;
        }

        boolean supportsSuper = false;
        for (Iterator<String> it = ancestors.iterator(); it.hasNext(); ) {
            String superClass = it.next();
            javaClassname = SandboxStringUtils.toJavaClassName(superClass);
            if (IastHookRuleModel.classIsNeededHookBySuperClassName(javaClassname)) {
                supportsSuper = true;
                break;
            }
        }
        return supportsSuper ? javaClassname : null;
    }

    public class ClassVisit extends AbstractClassVisitor {
        private final boolean isAppClass;
        private int classVersion;

        ClassVisit(ClassVisitor classVisitor, IastContext context) {
            super(classVisitor, context);
            String classname = context.getClassName();
            this.isAppClass = ConfigMatcher.isAppClass(classname);
        }

        @Override
        public boolean hasTransformed() {
            return transformed;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if (!Modifier.isInterface(access) && !Modifier.isAbstract(access) && !"<clinit>".equals(name)) {
                String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
                String framework = IastHookRuleModel.getFrameworkByMethodSignature(iastMethodSignature);

                mv = context.isEnableAllHook() ? greedyAop(mv, access, name, desc, framework == null ? "none" : framework, iastMethodSignature) : (framework == null ? mv : lazyAop(mv, access, name, desc, framework, iastMethodSignature));

                if (transformed && this.classVersion < 50) {
                    mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
                }

                if (transformed && logger.isDebugEnabled()) {
                    logger.debug("rewrite method {} for listener[framework={},class={}]", iastMethodSignature, framework, context.getClassName());
                }
            }
            return mv;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
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
        private MethodVisitor greedyAop(MethodVisitor mv, int access, String name, String desc, String framework, String signature) {
            if (null != framework) {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, signature);
            } else if (isAppClass && Method.hook(access, name, desc, signature)) {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, signature);
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
        private MethodVisitor lazyAop(MethodVisitor mv, int access, String name, String desc, String framework, String signature) {
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
                    logger.error("framework[{}], method[{}] doesn't find sink model", framework, name);
                }
            }
            return mv;
        }
    }
}
