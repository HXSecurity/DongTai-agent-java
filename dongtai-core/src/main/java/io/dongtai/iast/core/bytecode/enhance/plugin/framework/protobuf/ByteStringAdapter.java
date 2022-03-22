package io.dongtai.iast.core.bytecode.enhance.plugin.framework.protobuf;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ByteStringAdapter extends AbstractClassVisitor {
    public ByteStringAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("toStringUtf8")) {
            mv = new ByteStringAdviceAdapter(mv, access, name, descriptor);
            transformed = true;
        }
        return mv;
    }
}
