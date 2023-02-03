package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchJ2ee implements DispatchPlugin {

    private final String FILTER = " javax.servlet.Filter".substring(1);
    private final String FILTER_CHAIN = " javax.servlet.FilterChain".substring(1);
    private final String HTTP_SERVLET = " javax.servlet.http.HttpServlet".substring(1);
    private final String JAKARTA_SERVLET = " jakarta.servlet.http.HttpServlet".substring(1);
    private final String FACES_SERVLET = " javax.faces.webapp.FacesServlet".substring(1);


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();
        Set<String> ancestors = context.getAncestors();

        if (isServletDispatch(className, ancestors) || isJakartaServlet(className)) {
            classVisitor = new ServletDispatcherAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    private boolean isServletDispatch(String className, Set<String> diagram) {
        boolean isServlet = FACES_SERVLET.equals(className);
        isServlet = (isServlet || HTTP_SERVLET.equals(className));
        return (isServlet || diagram.contains(FILTER) || diagram.contains(FILTER_CHAIN));
    }

    private boolean isJakartaServlet(String className) {
        return JAKARTA_SERVLET.equals(className);
    }
}
