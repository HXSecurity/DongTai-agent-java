package com.secnium.iast.core.enhance.plugins.framework.struts2;


import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractAdviceAdapter;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Struts2InvocationClassAdapter extends AbstractClassVisitor implements Opcodes {

    public Struts2InvocationClassAdapter(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("invokeAction".equals(name) && "(Ljava/lang/Object;Lcom/opensymphony/xwork2/config/entities/ActionConfig;)Ljava/lang/String;".equals(desc)) {
            mv = new Struts2InvocationAdviceAdapter(mv, access, name, desc, AsmUtils.buildSignature(context.getClassName(), name, desc), context);
            transformed = true;
        }
        return mv;
    }

    // fixme: 增加struts2的反射处理逻辑
    private static class Struts2InvocationAdviceAdapter extends AbstractAdviceAdapter {

        Struts2InvocationAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String desc, String signCode, IASTContext context) {
            super(methodVisitor, access, name, desc, context, "Struts2", signCode);
        }

        @Override
        protected void before() {

        }

        @Override
        protected void after(int opcode) {

        }
    }
}
