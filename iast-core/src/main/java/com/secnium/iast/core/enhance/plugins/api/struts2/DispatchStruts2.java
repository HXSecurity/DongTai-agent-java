package com.secnium.iast.core.enhance.plugins.api.struts2;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchStruts2 implements DispatchPlugin {
    private String classname;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classname = context.getClassName();
        String struts2Classname = isMatch();
        if (struts2Classname != null) {
            classVisitor = new Struts2Adapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        String strutsClassname = " org.apache.struts2.dispatcher.Dispatcher".substring(1);
        if (strutsClassname.equals(classname)) {
            return strutsClassname;
        } else return null;
    }
}
