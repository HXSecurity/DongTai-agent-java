package io.dongtai.iast.core.bytecode.enhance.plugin.authentication.shiro;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchShiro implements DispatchPlugin {

    private static final String FRAMEWORK_SERVLET = " org.apache.shiro.session.mgt.eis.AbstractSessionDAO".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();
        if (FRAMEWORK_SERVLET.equals(className)) {
            classVisitor = new ShiroAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String getName() {
        return "shiro";
    }
}
