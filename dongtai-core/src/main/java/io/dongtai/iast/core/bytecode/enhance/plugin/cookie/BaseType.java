package io.dongtai.iast.core.bytecode.enhance.plugin.cookie;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.PropagateAdviceAdapter;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.SinkAdviceAdapter;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

/**
 * Java类型 增强器，目前支持: Collection、Map、Map.entry、List、Enumeration
 * 待添加: String
 *
 * @author dongzhiyong@huoxian.cn
 */
public class BaseType extends AbstractClassVisitor {
    private final List<String> hookMethods;

    public BaseType(ClassVisitor classVisitor, ClassContext context, List<String> methods) {
        super(classVisitor, context);
        this.hookMethods = methods;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (match(name, context.getMatchedClassName())) {
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchedClassName(), name, desc);
            String framework = "refType";
            if (iastMethodSignature.contains("setSecure") || iastMethodSignature.contains("<init>")) {
                mv = new SinkAdviceAdapter(mv, access, name, desc, context, framework, iastMethodSignature, false);
            } else {
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, iastMethodSignature);
            }
            setTransformed();
            DongTaiLog.trace("rewrite method {} for listener[match={},class={}]", iastMethodSignature, context.getMatchedClassName(), context.getClassName());
        }
        return mv;
    }

    protected boolean match(String name, String classname) {
        return hookMethods.contains(name);
    }
}
