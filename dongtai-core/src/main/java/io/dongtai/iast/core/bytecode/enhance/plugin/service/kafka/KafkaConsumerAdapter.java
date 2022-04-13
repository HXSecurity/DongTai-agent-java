package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.*;

public class KafkaConsumerAdapter extends AbstractClassVisitor {
    private String classDesc;

    public KafkaConsumerAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        int argCount = Type.getArgumentTypes(desc).length;

        if ("<init>".equals(name) && "(Ljava/util/Map;Lorg/apache/kafka/common/serialization/Deserializer;Lorg/apache/kafka/common/serialization/Deserializer;)V".equals(desc)) {
            DongTaiLog.debug("Adding kafka tracking for type {}.{}", context.getClassName(), name);
            mv = new KafkaConsumerAdviceAdapter(mv, access, name, desc);
            setTransformed();
        } else if ("poll".equals(name) && argCount == 2) {
            DongTaiLog.debug("Adding kafka tracking for type {}.{}", context.getClassName(), name);
            mv = new KafkaConsumerPollAdviceAdapter(mv, access, name, desc);
            setTransformed();
        }
        return mv;
    }
}
