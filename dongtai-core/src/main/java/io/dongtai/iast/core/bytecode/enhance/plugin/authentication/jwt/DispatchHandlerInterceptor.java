package io.dongtai.iast.core.bytecode.enhance.plugin.authentication.jwt;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

import java.util.Set;

public class DispatchHandlerInterceptor implements DispatchPlugin {

    private static final String INTERFACE_HANDLER_SPRING = " org.springframework.web.servlet.HandlerInterceptor".substring(1);
    private Set<String> ancestors;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ancestors = context.getAncestors();
        String supportedClassName = isMatch();
        if (supportedClassName != null) {
            classVisitor = new HandlerInterceptorAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        for (String superClassName : ancestors) {
            if (hookBySuperClass(superClassName)) {
                return superClassName;
            }
        }
        return null;
    }

    public static boolean hookBySuperClass(String classname) {
        if (classname != null) {
            return INTERFACE_HANDLER_SPRING.equals(classname);
        } else {
            return false;
        }
    }

}
