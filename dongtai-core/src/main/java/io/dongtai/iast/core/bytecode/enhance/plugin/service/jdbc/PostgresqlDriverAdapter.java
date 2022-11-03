package io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class PostgresqlDriverAdapter extends AbstractClassVisitor {
    public PostgresqlDriverAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if ("parseURL".equals(name)) {
            DongTaiLog.debug("Adding PostgreSQL jdbc tracking for type {}.{}", context.getClassName(), name);

            mv = new PostgresqlDriverParseUrlAdviceAdapter(mv, access, name, desc);
            setTransformed();
        }
        return mv;
    }
}
