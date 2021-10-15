package com.secnium.iast.core.enhance.plugins.api.spring;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class SpringApplicationAdapter extends AbstractClassVisitor {

    public SpringApplicationAdapter(ClassVisitor classVisitor, IastContext context) {
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
        if ("getWebApplicationContext".equals(name)) {
            methodVisitor = new SpringApplicationAdviceAdapter(
                    methodVisitor,
                    access,
                    name,
                    descriptor,
                    context,
                    "SPRINGAPPLICATION_FOR_API",
                    "SPRINGAPPLICATION_FOR_API"
            );
            transformed = true;
        }
        return methodVisitor;

    }
}



