package io.dongtai.iast.core.bytecode.enhance.plugin.authentication.jwt;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.SpringApplicationAdviceAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class HandlerInterceptorAdapter extends AbstractClassVisitor {

    public HandlerInterceptorAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access,
                name,
                descriptor,
                signature,
                exceptions);
        if ("preHandle".equals(name)) {
            methodVisitor = new HandlerInterceptorAdviceAdapter(
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



