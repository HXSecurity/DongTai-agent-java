package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import com.secnium.iast.core.util.LogUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DubboAdapter extends AbstractClassVisitor {
    private final Logger logger = LogUtils.getLogger(getClass());

    public DubboAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if ("invoke".equals(name)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding Dubbo Source tracking for type {}", context.getClassName());
            }

            mv = new DubboAdviceAdapter(mv, access, name, desc, signCode, context);
            transformed = true;
        }
        if (transformed) {
            if (logger.isDebugEnabled()) {
                logger.debug("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchClassName());
            }
        }
        return mv;
    }
}
