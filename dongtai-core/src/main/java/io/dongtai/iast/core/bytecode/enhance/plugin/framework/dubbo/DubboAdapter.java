package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DubboAdapter extends AbstractClassVisitor {

    public DubboAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if ("invoke".equals(name)) {
            DongTaiLog.trace("Adding Dubbo Source tracking for type {}", context.getClassName());
            mv = new DubboAdviceAdapter(mv, access, name, desc, signCode, context);
            setTransformed();
            DongTaiLog.trace("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchedClassName());
        }
        return mv;
    }
}
