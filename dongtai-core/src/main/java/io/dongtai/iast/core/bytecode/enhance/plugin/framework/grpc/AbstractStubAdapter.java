package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.*;

public class AbstractStubAdapter extends AbstractClassVisitor {
    public AbstractStubAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        int argCount = Type.getArgumentTypes(descriptor).length;
        if (name.equals("<init>") && argCount == 2) {
            mv = new AbstractStubAdviceAdapter(mv, access, name, descriptor);
            setTransformed();
        }
        return mv;
    }
}
