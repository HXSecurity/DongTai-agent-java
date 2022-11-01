package io.dongtai.iast.core.bytecode.enhance.plugin.spring;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class SpringApplicationAdapter extends AbstractClassVisitor {

    public SpringApplicationAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
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
            setTransformed();
        }
        return methodVisitor;

    }
}



