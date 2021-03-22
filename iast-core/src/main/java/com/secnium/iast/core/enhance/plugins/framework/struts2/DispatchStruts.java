package com.secnium.iast.core.enhance.plugins.framework.struts2;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import com.secnium.iast.core.util.commonUtils;
import org.objectweb.asm.ClassVisitor;

public class DispatchStruts implements DispatchPlugin {

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IASTContext context) {
        String classname = context.getClassName();
        boolean actionSupport = context.getAncestors().contains(baseClass) && !ignoreEquals(classname);
        boolean actionInvocation = !actionSupport && classname.equals(invocationClass);

        if (actionSupport) {
            classVisitor = new Struts2ActionClassVisitor(classVisitor, context);
        } else if (actionInvocation) {
            classVisitor = new Struts2InvocationClassAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

    private boolean ignoreEquals(String classname) {
        return commonUtils.arrayEquals(ignoreClass, classname);
    }

    private static final String invocationClass = " com/opensymphony/xwork2/DefaultActionInvocation".substring(1);

    private static final String baseClass = " com/opensymphony/xwork2/ActionSupport".substring(1);

    private static final String[] ignoreClass = {
            " org/apache/struts2/dispatcher/DefaultActionSupport".substring(1),
            " org/apache/struts2/rest/RestActionSupport".substring(1)
    };
}
