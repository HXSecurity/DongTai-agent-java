package io.dongtai.iast.core.bytecode.enhance.plugin.cookie;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.adapter.PropagateAdviceAdapter;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import io.dongtai.log.DongTaiLog;

import java.util.List;

/**
 * Java类型 增强器，目前支持: Collection、Map、Map.entry、List、Enumeration
 * 待添加: String
 *
 * @author dongzhiyong@huoxian.cn
 */
public class BaseType extends AbstractClassVisitor {
    private final List<String> hookMethods;

    public BaseType(ClassVisitor classVisitor, IastContext context, List<String> methods) {
        super(classVisitor, context);
        this.hookMethods = methods;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (match(name, context.getMatchClassName())) {
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassName(), name, desc);
            String framework = "refType";
            mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, iastMethodSignature);
            transformed = true;
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("rewrite method {} for listener[match={},class={}]", iastMethodSignature, context.getMatchClassName(), context.getClassName());
            }
        }
        return mv;
    }

    protected boolean match(String name, String classname) {
        return hookMethods.contains(name);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }
}
