package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;

import java.lang.reflect.Modifier;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import io.dongtai.log.DongTaiLog;

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
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        Set<String> diagram = context.getAncestors();

        if (Modifier.isInterface(context.getFlags())) {
            DongTaiLog.trace("Ignoring interface " + className);
        } else if (isServletDispatch(className, diagram) || isJakartaServlet(className)) {
            classVisitor = new ServletDispatcherAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
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
