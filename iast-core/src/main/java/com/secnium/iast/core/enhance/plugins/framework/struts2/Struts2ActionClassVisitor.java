package com.secnium.iast.core.enhance.plugins.framework.struts2;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;

public class Struts2ActionClassVisitor extends AbstractClassVisitor implements Opcodes {


    Struts2ActionClassVisitor(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if ((!Modifier.isAbstract(access) && !Modifier.isNative(access)) && Modifier.isPublic(access) && !"<init>".equals(name)) {
            mv = new Struts2ActionAdviceAdapter(mv, access, name, desc, AsmUtils.buildSignature(context.getClassName(), name, desc), context);
            transformed = true;
        }
        return mv;
    }

    // fixme: 增加struts2的action处理逻辑
    private static class Struts2ActionAdviceAdapter extends AbstractAdviceAdapter {

        Struts2ActionAdviceAdapter(MethodVisitor methodVisitor,
                                   int access,
                                   String name,
                                   String descriptor,
                                   String signCode,
                                   IASTContext context) {
            super(methodVisitor, access, name, descriptor, context, "struts2", signCode);
        }

        @Override
        protected void before() {

        }

        @Override
        protected void after(int opcode) {

        }
    }
}
