package io.dongtai.iast.core.bytecode.enhance.plugin.framework.tomcat;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import io.dongtai.log.DongTaiLog;

import java.lang.reflect.Modifier;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class CharChunkVisitor extends AbstractClassVisitor {

    private IastContext IastContext;

    public CharChunkVisitor(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
        this.IastContext = context;
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        int bool = (!Modifier.isNative(access) && !Modifier.isAbstract(access)) ? 1 : 0;
        if (1 == bool && "recycle".equals(name)) {
            if (DongTaiLog.isDebugEnabled()) {
                DongTaiLog.debug("Instrumenting Tomcat's ByteChunk recycle() method");
            }
            mv = new CharChunkAdapter(mv, access, name, desc, IastContext);
            transformed = true;
        }
        return mv;
    }

}
