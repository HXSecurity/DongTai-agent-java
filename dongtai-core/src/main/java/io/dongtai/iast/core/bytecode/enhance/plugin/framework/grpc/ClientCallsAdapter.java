package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ClientCallsAdapter extends AbstractClassVisitor {
    public ClientCallsAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        int argCount = Type.getArgumentTypes(descriptor).length;
        if (name.equals("blockingUnaryCall") && argCount == 4) {
            mv = new ClientCallsAdviceAdapter(mv, access, name, descriptor);
            setTransformed();
        }
        return mv;
    }
}
