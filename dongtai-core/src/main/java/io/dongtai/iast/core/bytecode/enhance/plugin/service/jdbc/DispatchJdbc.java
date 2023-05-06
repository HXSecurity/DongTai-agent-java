package io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchJdbc implements DispatchPlugin {
    // mysql 5.x
    private final String classOfMysqlJdbcDriver = " com.mysql.jdbc.NonRegisteringDriver".substring(1);
    // mysql 8.x
    private final String classOfMysqlHostInfo = " com.mysql.cj.conf.HostInfo".substring(1);
    // postgresql
    private final String classOfPostgresqlDriver = " org.postgresql.Driver".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (classOfMysqlJdbcDriver.equals(className)) {
            classVisitor = new MysqlJdbcDriverAdapter(classVisitor, context);
        } else if (classOfMysqlHostInfo.equals(className)) {
            classVisitor = new MysqlHostInfoAdapter(classVisitor, context);
        } else if (classOfPostgresqlDriver.equals(className)) {
            classVisitor = new PostgresqlDriverAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String getName() {
        return "jdbc";
    }
}
