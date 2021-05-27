package com.secnium.iast.core.enhance.plugins.sources.servlet;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.enhance.plugins.sinks.SinkAdviceAdapter;
import com.secnium.iast.core.util.AsmUtils;
import com.secnium.iast.core.util.LogUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ServletClassVisitor extends AbstractClassVisitor {
    private final Logger logger = LogUtils.getLogger(ServletClassVisitor.class);
    private static final Set<String> HOOK_METHODS = new HashSet<String>(Arrays.asList(
            "getParameterMap",
            "getParameterNames",
            "getQueryString",
            "getCookies",
            "getInputStream",
            "getParameter",
            "getParameterValues",
            "getHeader",
            "getHeaders",
            "getHeaderNames",
            "getParts",
            "getPart"
//            "getAttribute",
//            "getAttributeNames",
            // "getRealPath"
    ));

    private static final Set<String> STREAM_METHODS = new HashSet<String>(Arrays.asList(
            "getReader",
            "getInputStream"
    ));

    private static final String SERVLET_REQUEST = " javax.servlet.ServletRequest.getRequestDispatcher(java.lang.String)".substring(1);

    public ServletClassVisitor(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        boolean methodTansformed = false;
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String iastMethodSignature = null;
        if (HOOK_METHODS.contains(name)) {
            iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            mv = new ServletAdviceAdapter(mv, access, name, desc, context, iastMethodSignature);
            transformed = true;
            methodTansformed = true;
        } else if (STREAM_METHODS.contains(name)) {
            iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            mv = new ServletStreamAdviceAdapter(mv, access, name, desc, context, iastMethodSignature);
            transformed = true;
            methodTansformed = true;
        } else if ("getRequestDispatcher".equals(name)) {
            iastMethodSignature = SERVLET_REQUEST;
            mv = new SinkAdviceAdapter(mv, access, name, desc, context, "unvalidated-forward", iastMethodSignature, false);
            transformed = true;
            methodTansformed = true;
        }
        if (methodTansformed) {
            logger.debug("rewrite method {} for listener[framwrork=source,class={}]", iastMethodSignature, context.getClassName());
        }
        return mv;
    }
}
