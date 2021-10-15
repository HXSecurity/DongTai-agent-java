package com.secnium.iast.core.enhance.plugins.api.struts2;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class Struts2Adapter extends AbstractClassVisitor {
    public Struts2Adapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access,
                name,
                descriptor,
                signature,
                exceptions);
        if ("getInstance".equals(name)) {
            methodVisitor = new Struts2AdviceAdapter(
                    methodVisitor,
                    access,
                    name,
                    descriptor,
                    context,
                    "STRUTS2_FOR_API",
                    "STRUTS2_FOR_API"
            );
            transformed = true;
        }
        return methodVisitor;

    }
}
