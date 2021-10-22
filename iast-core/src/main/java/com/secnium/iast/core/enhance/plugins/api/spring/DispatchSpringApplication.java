package com.secnium.iast.core.enhance.plugins.api.spring;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchSpringApplication implements DispatchPlugin {

    private String classname;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classname = context.getClassName();
        String springbootClassname = isMatch();
        if (springbootClassname != null) {
            classVisitor = new SpringApplicationAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        String springbootClassname = " org.springframework.web.servlet.FrameworkServlet".substring(1);
        if (springbootClassname.equals(classname)) {
            return springbootClassname;
        } else return null;
    }

}
