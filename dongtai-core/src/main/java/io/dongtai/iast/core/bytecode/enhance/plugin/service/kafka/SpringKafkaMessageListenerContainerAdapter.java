package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class SpringKafkaMessageListenerContainerAdapter extends AbstractClassVisitor {
    public SpringKafkaMessageListenerContainerAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        // >=1.3 argCount=2
        // >=2.0 argCount=3
        // >=2.5 argCount=2
        if ("doInvokeRecordListener".equals(name)) {
            DongTaiLog.debug("Adding spring kafka tracking for type {}.{}", context.getClassName(), name);
            mv = new SpringKafkaMessageListenerContainerAdviceAdapter(mv, access, name, desc);
            setTransformed();
        }
        return mv;
    }
}
