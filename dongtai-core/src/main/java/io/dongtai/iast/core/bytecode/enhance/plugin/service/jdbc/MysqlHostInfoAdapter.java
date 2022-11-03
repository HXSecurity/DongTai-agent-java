package io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;

public class MysqlHostInfoAdapter extends AbstractClassVisitor {
    public MysqlHostInfoAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        int argCount = Type.getArgumentTypes(desc).length;

        if ("<init>".equals(name) && argCount == 7) {
            DongTaiLog.debug("Adding MySQL jdbc tracking for type {}.{}", context.getClassName(), name);

            mv = new MysqlHostInfoAdviceAdapter(mv, access, name, desc);
            setTransformed();
        }
        return mv;
    }
}
