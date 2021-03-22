package com.secnium.iast.core.enhance.plugins.sources.servlet.stream.catalina;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CoyoteInputStreamClassVisitor extends AbstractClassVisitor {
    public CoyoteInputStreamClassVisitor(ClassVisitor classVisitor, IASTContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("read".equals(name)) {
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
            //
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
