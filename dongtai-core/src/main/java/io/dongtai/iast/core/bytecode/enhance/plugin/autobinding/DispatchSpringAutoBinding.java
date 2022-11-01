package io.dongtai.iast.core.bytecode.enhance.plugin.autobinding;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchSpringAutoBinding implements DispatchPlugin {
    static String autobindClassname = " org/springframework/web/servlet/mvc/annotation/AnnotationMethodHandlerAdapter$ServletHandlerMethodInvoker".substring(1);
    String classname;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
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
