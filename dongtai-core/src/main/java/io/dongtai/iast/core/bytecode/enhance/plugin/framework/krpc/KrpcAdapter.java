package io.dongtai.iast.core.bytecode.enhance.plugin.framework.krpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class KrpcAdapter extends AbstractClassVisitor {
    public KrpcAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return false;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if ("invoke".equals(name)) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("Adding Krpc Source tracking for type {}", context.getClassName());
            }

            mv = new KrpcAdviceAdapter(mv, access, name, desc, signCode, context);
            transformed = true;
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchClassName());
            }
        }
        return mv;
    }

}
