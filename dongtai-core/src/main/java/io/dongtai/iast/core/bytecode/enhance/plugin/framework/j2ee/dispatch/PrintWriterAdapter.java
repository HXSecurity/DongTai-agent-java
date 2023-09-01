package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;

import java.util.*;

public class PrintWriterAdapter extends AbstractClassVisitor {
    private static final Set<String> WRITE_DESC = new HashSet<>(Arrays.asList("(I)V", "([CII)V", "(Ljava/lang/String;II)V"));

    PrintWriterAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        Type[] typeOfArgs = Type.getArgumentTypes(desc);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);
        if (isWrite(name, desc)) {
            DongTaiLog.debug("Adding HTTP response PrintWriter by {}", signCode);
            mv = new PrintWriterWriteAdviceAdapter(mv, access, name, desc, signCode, context);
            setTransformed();
        }
        if (hasTransformed()) {
            DongTaiLog.trace("rewrite method {}.{} for listener[match={}]", context.getClassName(), name, context.getMatchedClassName());
        }

        return mv;
    }

    private boolean isWrite(String name, String desc) {
        return "write".equals(name) && WRITE_DESC.contains(desc);
    }
}
