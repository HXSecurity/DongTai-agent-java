package io.dongtai.iast.core.bytecode.enhance.plugin.cookie;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import io.dongtai.log.DongTaiLog;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchCookie implements DispatchPlugin {

    private static final String SERVLET_COOKIE = " javax.servlet.http.Cookie".substring(1);
    private static final String GLASSFISH_COOKIE = " org.glassfish.grizzly.http.Cookie".substring(1);
    private static final String WS_COOKIE = " javax.ws.rs.core.NewCookie".substring(1);
    private static String classname;
    private static Set<String> ancestors;


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        ancestors = context.getAncestors();
        classname = context.getClassName();
        String matchClassname = isMatch();
        if (null != matchClassname) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("Cookie match class for {} from {}", classname, matchClassname);
            }
            context.setMatchClassName(matchClassname);
            classVisitor = new CookieAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (ancestors.contains(SERVLET_COOKIE)) {
            return SERVLET_COOKIE;
        } else if (classname.equals(GLASSFISH_COOKIE) || ancestors.contains(GLASSFISH_COOKIE)) {
            return GLASSFISH_COOKIE;
        } else if (classname.equals(WS_COOKIE) || ancestors.contains(WS_COOKIE)) {
            return WS_COOKIE;
        } else {
            return null;
        }
    }

    static boolean isHookMethod(String name) {
        if ((classname.equals(SERVLET_COOKIE) || ancestors.contains(SERVLET_COOKIE)) && ("setSecure".equals(name)
                || "getValue".equals(name))) {
            return true;
        } else if ((classname.equals(GLASSFISH_COOKIE) || ancestors.contains(GLASSFISH_COOKIE)) && "setSecure"
                .equals(name)) {
            return true;
        } else {
            return (classname.equals(WS_COOKIE) || ancestors.contains(WS_COOKIE)) && "<init>".equals(name);
        }
    }


}
