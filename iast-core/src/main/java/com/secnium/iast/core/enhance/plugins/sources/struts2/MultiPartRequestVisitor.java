package com.secnium.iast.core.enhance.plugins.sources.struts2;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.enhance.plugins.sources.servlet.ServletAdviceAdapter;
import com.secnium.iast.core.enhance.plugins.sources.servlet.ServletClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MultiPartRequestVisitor extends AbstractClassVisitor {
    private final Logger logger = LoggerFactory.getLogger(ServletClassVisitor.class);
    private static final Set<String> HOOK_METHODS = new HashSet<String>(Arrays.asList(
            "getFileNames",
            "getParameter",
            "getParameterNames",
            "getParameterValues"
    ));

    public MultiPartRequestVisitor(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String iastMethodSignature = null;

        if (HOOK_METHODS.contains(name)) {
            iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            mv = new ServletAdviceAdapter(mv, access, name, desc, context, iastMethodSignature);
            transformed = true;
        }
        return mv;
    }
}
