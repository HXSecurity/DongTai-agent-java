package com.secnium.iast.core.enhance.plugins.api;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.enhance.plugins.core.adapter.PropagateAdviceAdapter;
import com.secnium.iast.core.handler.controller.HookType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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
        if ("run".equals(name) && Type.getArgumentTypes(descriptor).length == 1) {
            System.out.println(context.getClassName());
//            methodVisitor = new SpringApplicationAdviceAdapter(methodVisitor,
//                    access,
//                    name,
//                    descriptor,
//                    context,
//                    "spring",
//                    "signature"
//            );
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



