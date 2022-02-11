package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import io.dongtai.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletDispatcherAdapter extends AbstractClassVisitor {

    private final String HTTP_SERVLET_REQUEST = " javax.servlet.http.HttpServletRequest".substring(1);
    private final String HTTP_SERVLET_RESPONSE = " javax.servlet.http.HttpServletResponse".substring(1);
    private final String SERVLET_REQUEST = " javax.servlet.ServletRequest".substring(1);
    private final String SERVLET_RESPONSE = " javax.servlet.ServletResponse".substring(1);
    private final String FILTER_CHAIN = " javax.servlet.FilterChain".substring(1);
    private final String JAKARTA_SERVLET_REQUEST = " jakarta.servlet.http.HttpServletRequest".substring(1);
    private final String JAKARTA_SERVLET_RESPONSE = " jakarta.servlet.http.HttpServletResponse".substring(1);


    private final boolean isFaces;
    private final boolean isJakarta;

    ServletDispatcherAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
        this.isFaces = " javax.faces.webapp.FacesServlet".substring(1).equals(context.getClassName());
        this.isJakarta = " jakarta.servlet.http.HttpServlet".substring(1).equals(context.getClassName());
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        Type[] typeOfArgs = Type.getArgumentTypes(desc);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if (isService(name, typeOfArgs) ||
                (this.isJakarta && isJakartaArgs(typeOfArgs)) ||
                (this.isFaces && isFacesArgs(typeOfArgs))
        ) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("Adding HTTP tracking for type {}", context.getClassName());
            }

            mv = new ServletDispatcherAdviceAdapter(mv, access, name, desc, signCode, context, isJakarta);
            transformed = true;
        }
        if (transformed) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchClassName());
            }
        }

        return mv;
    }

    /**
     * 检查是否为http入口方法（service/doFilter)
     * javax.servlet.http.HttpServlet
     *
     * @param name       方法名称
     * @param typeOfArgs 方法参数
     * @return true-是http入口方法，falst-非http入口方法
     */
    private boolean isService(String name, Type[] typeOfArgs) {
        if ("service".equals(name)) {
            return isServiceArgs(typeOfArgs);
        } else {
            return "doFilter".equals(name) && (isFilterArg(typeOfArgs) || isFilterChainArg(typeOfArgs));
        }

    }

    private boolean isServiceArgs(Type[] typeOfArgs) {
        return typeOfArgs.length == 2 &&
                HTTP_SERVLET_REQUEST.equals(typeOfArgs[0].getClassName()) &&
                HTTP_SERVLET_RESPONSE.equals(typeOfArgs[1].getClassName());
    }

    private boolean isJakartaArgs(Type[] typeOfArgs) {
        return typeOfArgs.length == 2 &&
                JAKARTA_SERVLET_REQUEST.equals(typeOfArgs[0].getClassName()) &&
                JAKARTA_SERVLET_RESPONSE.equals(typeOfArgs[1].getClassName());
    }

    private boolean isFacesArgs(Type[] typeOfArgs) {
        if (typeOfArgs.length != 2) {
            return false;
        }
        String arg1Classname = typeOfArgs[0].getClassName();
        String arg2Classname = typeOfArgs[1].getClassName();
        return SERVLET_REQUEST.equals(arg1Classname) && SERVLET_RESPONSE.equals(arg2Classname);
    }

    private boolean isFilterArg(Type[] typeOfArgs) {
        return typeOfArgs.length == 3 &&
                SERVLET_REQUEST.equals(typeOfArgs[0].getClassName()) &&
                SERVLET_RESPONSE.equals(typeOfArgs[1].getClassName()) &&
                FILTER_CHAIN.equals(typeOfArgs[2].getClassName());
    }

    private boolean isFilterChainArg(Type[] typeOfArgs) {
        return typeOfArgs.length == 2 &&
                SERVLET_REQUEST.equals(typeOfArgs[0].getClassName()) &&
                SERVLET_RESPONSE.equals(typeOfArgs[1].getClassName());
    }
}
