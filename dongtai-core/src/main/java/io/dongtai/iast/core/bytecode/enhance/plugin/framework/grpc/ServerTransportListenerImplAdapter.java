package io.dongtai.iast.core.bytecode.enhance.plugin.framework.grpc;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ServerTransportListenerImplAdapter extends AbstractClassVisitor {
    public ServerTransportListenerImplAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("startCall")) {
            mv = new ServerTransportListenerImplAdviceAdapter(mv, access, name, descriptor);
            setTransformed();
        }
        return mv;
    }
}
