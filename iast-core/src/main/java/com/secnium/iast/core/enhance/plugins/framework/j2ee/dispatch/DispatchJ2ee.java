package com.secnium.iast.core.enhance.plugins.framework.j2ee.dispatch;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchJ2ee implements DispatchPlugin {
    private final Logger logger = LogUtils.getLogger(getClass());
    private final String FILTER = " javax/servlet/Filter".substring(1);
    private final String FILTERCHAIN = " javax/servlet/FilterChain".substring(1);
    private final String HTTP_SERVLET = " javax/servlet/http/HttpServlet".substring(1);
    private final String HTTP_SERVLET_RESPONSE = " javax/servlet/http/HttpServletResponse".substring(1);
    private final String HTTP_SERVLET_REQUEST = " javax/servlet/http/HttpServletRequest".substring(1);
    private final String SERVLET_INPUT_STREAM = " javax/servlet/ServletInputStream".substring(1);
    private final String SERVLET_OUTPUT_STREAM = " javax/servlet/ServletOutputStream".substring(1);
    private final String HTTP_PART = " javax/servlet/http/Part".substring(1);
    private final String PRINT_WRITER = " java/io/PrintWriter".substring(1);
    private final String JSP_WRITER_IMPL = " weblogic/servlet/jsp/JspWriterImpl".substring(1);
    private final String FACES_SERVLET = " javax/faces/webapp/FacesServlet".substring(1);


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();
        HashSet<String> ancestors = context.getAncestors();

        if (Modifier.isInterface(context.getFlags())) {
            logger.trace("Ignoring interface " + className);
        } else if (isServletDispatch(className, ancestors)) {
            //ServletDispatcherAdapter
            classVisitor = new ServletDispatcherAdapter(classVisitor, context);
        } else {
            // ResponseAdapter
            if (ancestors.contains(HTTP_SERVLET_RESPONSE)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ResponseAdapter");
                }
            } else {
                //ServletInputStreamAdapter
                if (ancestors.contains(SERVLET_INPUT_STREAM)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ServletInputStreamAdapter");
                    }
                } else {
                    //MultipartCapturingAdapter
                    if (ancestors.contains(HTTP_PART)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("MultipartCapturingAdapter");
                        }
                    } else if (PRINT_WRITER.equals(className) || ancestors.contains(PRINT_WRITER)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("PrintWriterAdapter");
                        }
                    } else {
                        if (SERVLET_OUTPUT_STREAM.equals(className) || ancestors.contains(SERVLET_OUTPUT_STREAM)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("ServletOutputStreamAdapter");
                            }
                        } else if (JSP_WRITER_IMPL.equals(className)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("WebLogicJspWriterAdapter");
                            }
                        }
                    }
                }
            }
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

    private boolean isServletDispatch(String className, Set ancestors) {
        boolean isServlet = FACES_SERVLET.equals(className);
        isServlet = (isServlet || HTTP_SERVLET.equals(className));
        return (isServlet || ancestors.contains(FILTER) || ancestors.contains(FILTERCHAIN));
    }
}
