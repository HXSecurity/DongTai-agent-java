package com.secnium.iast.core.enhance.plugins.sources.spring;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class ArgumentResolverVisitor extends AbstractClassVisitor {
    public ArgumentResolverVisitor(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("resolveArgument".equals(name)) {
            // 创建方法访问器
            transformed = true;
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            mv = new ArgumentResolverAdviceAdapter(mv, access, name, desc, context, iastMethodSignature);
        }
        return mv;
    }
}
