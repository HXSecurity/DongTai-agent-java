package com.secnium.iast.core.enhance.plugins.sinks.cookie;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class DispatchCookie implements DispatchPlugin {
    private static String servletCookie;
    private static String glassfishCookie;
    private static String wsCookie;
    private static String classname;
    private static HashSet<String> ancestors;


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IASTContext context) {
        ancestors = context.getAncestors();
        classname = context.getClassName();
        String matchClassname = isMatch();
        if (null != matchClassname) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cookie match class for {} from {}", classname, matchClassname);
            }
            context.setMatchClassname(matchClassname);
            classVisitor = new CookieAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (ancestors.contains(servletCookie)) {
            return servletCookie;
        } else if (classname.equals(glassfishCookie) || ancestors.contains(glassfishCookie)) {
            return glassfishCookie;
        } else if (classname.equals(wsCookie) || ancestors.contains(wsCookie)) {
            return wsCookie;
        } else {
            return null;
        }
    }

    static boolean isHookMethod(String name) {
        if ((classname.equals(servletCookie) || ancestors.contains(servletCookie)) && ("setSecure".equals(name) || "getValue".equals(name))) {
            return true;
        } else if ((classname.equals(glassfishCookie) || ancestors.contains(glassfishCookie)) && "setSecure".equals(name)) {
            return true;
        } else {
            return (classname.equals(wsCookie) || ancestors.contains(wsCookie)) && "<init>".equals(name);
        }
    }

    static {
        servletCookie = " javax/servlet/http/Cookie".substring(1);
        glassfishCookie = " org/glassfish/grizzly/http/Cookie".substring(1);
        wsCookie = " javax/ws/rs/core/NewCookie".substring(1);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
}
