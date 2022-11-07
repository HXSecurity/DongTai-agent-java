package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;

public class KafkaAbstractConfigAdapter extends AbstractClassVisitor {
    public KafkaAbstractConfigAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        int argCount = Type.getArgumentTypes(desc).length;

        if ("<init>".equals(name) && argCount >= 3) {
            DongTaiLog.debug("Adding kafka tracking for type {}.{}", context.getClassName(), name);
            mv = new KafkaAbstractConfigInitAdviceAdapter(mv, access, name, desc);
            setTransformed();
        }
        return mv;
    }
}
