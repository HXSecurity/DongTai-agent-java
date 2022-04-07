package io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchJdbc implements DispatchPlugin {
    // mysql 5.x
    private final String classOfMysqlJdbcDriver = " com.mysql.jdbc.NonRegisteringDriver".substring(1);
    // mysql 8.x
    private final String classOfMysqlHostInfo = " com.mysql.cj.conf.HostInfo".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();

        if (classOfMysqlJdbcDriver.equals(className)) {
            classVisitor = new MysqlJdbcDriverAdapter(classVisitor, context);
        } else if (classOfMysqlHostInfo.equals(className)) {
            classVisitor = new MysqlHostInfoAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
