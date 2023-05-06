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
    public static final String JAVAX_HTTP_SERVLET = " javax.servlet.http.HttpServlet".substring(1);
    public static final String JAKARTA_HTTP_SERVLET = " jakarta.servlet.http.HttpServlet".substring(1);
    public static final String JAVAX_FILTER = " javax.servlet.Filter".substring(1);
    public static final String JAKARTA_FILTER = " jakarta.servlet.Filter".substring(1);
    public static final String JAVAX_FACES_SERVLET = " javax.faces.webapp.FacesServlet".substring(1);
    public static final String JAKARTA_FACES_SERVLET = " jakarta.faces.webapp.FacesServlet".substring(1);

    public static final String JAVAX_SERVLET_INPUT_STREAM = " javax.servlet.ServletInputStream".substring(1);
    public static final String JAKARTA_SERVLET_INPUT_STREAM = " jakarta.servlet.ServletInputStream".substring(1);
    public static final String JAVAX_SERVLET_OUTPUT_STREAM = " javax.servlet.ServletOutputStream".substring(1);
    public static final String JAKARTA_SERVLET_OUTPUT_STREAM = " jakarta.servlet.ServletOutputStream".substring(1);
    public static final String APACHE_COYOTE_WRITER = " org.apache.catalina.connector.CoyoteWriter".substring(1);
    public static final String UNDERTOW_SERVLET_WRITER = " io.undertow.servlet.spec.ServletPrintWriter".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();
        Set<String> ancestors = context.getAncestors();

        if (isJavaxServlet(className, ancestors)) {
            classVisitor = new ServletDispatcherAdapter(classVisitor, context, " javax".substring(1));
        } else if (isJakartaServlet(className, ancestors)) {
            classVisitor = new ServletDispatcherAdapter(classVisitor, context, " jakarta".substring(1));
        } else if (ancestors.contains(JAVAX_SERVLET_INPUT_STREAM)) {
            classVisitor = new ServletInputStreamAdapter(classVisitor, context);
        } else if (ancestors.contains(JAKARTA_SERVLET_INPUT_STREAM)) {
            classVisitor = new ServletInputStreamAdapter(classVisitor, context);
        } else if (ancestors.contains(JAVAX_SERVLET_OUTPUT_STREAM)) {
            classVisitor = new ServletOutputStreamAdapter(classVisitor, context);
        } else if (ancestors.contains(JAKARTA_SERVLET_OUTPUT_STREAM)) {
            classVisitor = new ServletOutputStreamAdapter(classVisitor, context);
        } else if (APACHE_COYOTE_WRITER.equals(className) || UNDERTOW_SERVLET_WRITER.equals(className)) {
            classVisitor = new PrintWriterAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String getName() {
        return "j2ee";
    }

    private boolean isJavaxServlet(String className, Set<String> diagram) {
        return JAVAX_FACES_SERVLET.equals(className) || JAVAX_HTTP_SERVLET.equals(className)
                || diagram.contains(JAVAX_HTTP_SERVLET) || diagram.contains(JAVAX_FILTER);
    }

    private boolean isJakartaServlet(String className, Set<String> diagram) {
        return JAKARTA_FACES_SERVLET.equals(className) || JAKARTA_HTTP_SERVLET.equals(className)
                || diagram.contains(JAKARTA_HTTP_SERVLET) || diagram.contains(JAKARTA_FILTER);
    }
}
