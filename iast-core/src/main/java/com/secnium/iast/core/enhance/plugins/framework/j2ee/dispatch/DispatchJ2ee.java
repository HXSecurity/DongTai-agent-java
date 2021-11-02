package com.secnium.iast.core.enhance.plugins.framework.j2ee.dispatch;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import com.secnium.iast.core.util.LogUtils;
import java.lang.reflect.Modifier;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchJ2ee implements DispatchPlugin {

    private final Logger logger = LogUtils.getLogger(getClass());
    private final String FILTER = " javax.servlet.Filter".substring(1);
    private final String FILTER_CHAIN = " javax.servlet.FilterChain".substring(1);
    private final String HTTP_SERVLET = " javax.servlet.http.HttpServlet".substring(1);
    private final String JAKARTA_SERVLET = " jakarta.servlet.http.HttpServlet".substring(1);
    private final String FACES_SERVLET = " javax.faces.webapp.FacesServlet".substring(1);


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        Set<String> ancestors = context.getAncestors();

        if (Modifier.isInterface(context.getFlags())) {
            logger.trace("Ignoring interface " + className);
        } else if (isServletDispatch(className, ancestors) || isJakartaServlet(className)) {
            classVisitor = new ServletDispatcherAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

    private boolean isServletDispatch(String className, Set<String> ancestors) {
        boolean isServlet = FACES_SERVLET.equals(className);
        isServlet = (isServlet || HTTP_SERVLET.equals(className));
        return (isServlet || ancestors.contains(FILTER) || ancestors.contains(FILTER_CHAIN));
    }

    private boolean isJakartaServlet(String className) {
        return JAKARTA_SERVLET.equals(className);
    }
}
