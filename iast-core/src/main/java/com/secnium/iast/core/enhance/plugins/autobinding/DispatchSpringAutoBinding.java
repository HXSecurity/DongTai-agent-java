package com.secnium.iast.core.enhance.plugins.autobinding;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchSpringAutoBinding implements DispatchPlugin {
    static String autobindClassname = " org/springframework/web/servlet/mvc/annotation/AnnotationMethodHandlerAdapter$ServletHandlerMethodInvoker".substring(1);
    String classname;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classname = context.getClassName();
        if (autobindClassname.equals(classname)) {
            classVisitor = new SpringAutoBindingAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

    public String getId() {
        return "spring-unchecked-autobinding";
    }
}
