package com.secnium.iast.core.enhance.plugins.cookie;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.enhance.plugins.core.adapter.PropagateAdviceAdapter;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

import java.util.List;

/**
 * Java类型 增强器，目前支持: Collection、Map、Map.entry、List、Enumeration
 * 待添加: String
 *
 * @author dongzhiyong@huoxian.cn
 */
public class BaseType extends AbstractClassVisitor {
    private final Logger logger = LogUtils.getLogger(getClass());

    private final List<String> hookMethods;

    public BaseType(ClassVisitor classVisitor, IastContext context, List<String> methods) {
        super(classVisitor, context);
        this.hookMethods = methods;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (match(name, context.getMatchClassname())) {
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            String framework = "refType";
            mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, iastMethodSignature);
            transformed = true;
            if (logger.isDebugEnabled()) {
                logger.debug("rewrite method {} for listener[id={},class={}]", iastMethodSignature, context.getListenId(), context.getClassName());
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
