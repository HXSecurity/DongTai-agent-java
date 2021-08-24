package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchSpringApplication implements DispatchPlugin {

    static String autoBindClassname = " org.springframework.web.servlet.FrameworkServlet".substring(1);

    private String classname;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classname = context.getClassName();
        if (autoBindClassname.equals(classname)) {
            classVisitor = new SpringApplicationAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

}
