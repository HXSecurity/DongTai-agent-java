package io.dongtai.iast.core.bytecode.enhance.plugin.spring;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchSpringApplication implements DispatchPlugin {

    private static final String FRAMEWORK_SERVLET = " org.springframework.web.servlet.FrameworkServlet".substring(1);
    private String className;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        className = context.getClassName();
        String supportedClassName = isMatch();
        if (supportedClassName != null) {
            classVisitor = new SpringApplicationAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (FRAMEWORK_SERVLET.equals(className)) {
            return FRAMEWORK_SERVLET;
        }
        return null;
    }

}
