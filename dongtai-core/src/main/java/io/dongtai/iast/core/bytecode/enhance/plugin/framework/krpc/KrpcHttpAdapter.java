package io.dongtai.iast.core.bytecode.enhance.plugin.framework.krpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class KrpcHttpAdapter extends AbstractClassVisitor {

    public KrpcHttpAdapter(ClassVisitor classVisitor, IastContext context) {
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
        if ("callService".equals(name)){
            mv = new KrpcHttpAdviceAdapter(mv, access, name, desc, signCode, context);
            transformed = true;
        }
        if ("startRender".equals(name)) {
            mv = new KrpcHttpExitAdviceAdapter(mv, access, name, desc, signCode, context);
            transformed = true;
        }
        return mv;
    }

}
