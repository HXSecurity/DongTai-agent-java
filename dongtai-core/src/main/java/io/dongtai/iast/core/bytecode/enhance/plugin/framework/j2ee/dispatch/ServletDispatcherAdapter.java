package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletDispatcherAdapter extends AbstractClassVisitor {
    public static final String JAVAX_FACES_WEBAPP_FACES_SERVLET = " javax.faces.webapp.FacesServlet".substring(1);

    private final String packageName;
    private final String httpServletRequest;
    private final String httpServletResponse;
    private final String servletRequest;
    private final String servletResponse;
    private final String filterChain;
    private final boolean isJavaxFacesServlet;

    ServletDispatcherAdapter(ClassVisitor classVisitor, ClassContext context, String packageName) {
        super(classVisitor, context);
        this.packageName = packageName;
        this.httpServletRequest = packageName + ".servlet.http.HttpServletRequest";
        this.httpServletResponse = packageName + ".servlet.http.HttpServletResponse";
        this.servletRequest = packageName + ".servlet.ServletRequest";
        this.servletResponse = packageName + ".servlet.ServletResponse";
        this.filterChain = packageName + ".servlet.FilterChain";
        this.isJavaxFacesServlet = JAVAX_FACES_WEBAPP_FACES_SERVLET.equals(context.getClassName());
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        Type[] typeOfArgs = Type.getArgumentTypes(desc);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if (isServiceOrFilter(name, typeOfArgs)) {
            DongTaiLog.debug("Adding HTTP tracking for {}", signCode);
            mv = new ServletDispatcherAdviceAdapter(mv, access, name, desc, signCode, context, packageName);
            setTransformed();
        } else if (this.isJavaxFacesServlet && isJavaxFacesServletArgs(typeOfArgs)) {
            DongTaiLog.debug("Adding HTTP tracking (FacesServlet hook) for {}", signCode);
            mv = new ServletDispatcherAdviceAdapter(mv, access, name, desc, signCode, context, packageName);
            setTransformed();
        }
        if (hasTransformed()) {
            DongTaiLog.trace("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchedClassName());
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
    private boolean isServiceOrFilter(String name, Type[] typeOfArgs) {
        if ("service".equals(name) && typeOfArgs.length == 2) {
            return isServiceArgs(typeOfArgs);
        } else if ("doFilter".equals(name) && typeOfArgs.length == 3) {
            return isDoFilterArg(typeOfArgs);
        }
        return false;
    }

    private boolean isServiceArgs(Type[] typeOfArgs) {
        String reqClassName = typeOfArgs[0].getClassName();
        String respClassName = typeOfArgs[1].getClassName();

        if (!this.httpServletRequest.equals(reqClassName) || !this.httpServletResponse.equals(respClassName)) {
            return this.servletRequest.equals(reqClassName) && this.servletResponse.equals(respClassName);
        }
        return true;
    }

    private boolean isJavaxFacesServletArgs(Type[] typeOfArgs) {
        if (typeOfArgs.length != 2) {
            return false;
        }
        String reqClassName = typeOfArgs[0].getClassName();
        String respClassName = typeOfArgs[1].getClassName();
        return this.servletRequest.equals(reqClassName) && this.servletResponse.equals(respClassName);
    }

    private boolean isDoFilterArg(Type[] typeOfArgs) {
        return this.servletRequest.equals(typeOfArgs[0].getClassName())
                && this.servletResponse.equals(typeOfArgs[1].getClassName())
                && this.filterChain.equals(typeOfArgs[2].getClassName());
    }
}
