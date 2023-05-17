package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchKafka implements DispatchPlugin {
    private final String classOfAbstractConfig = " org.apache.kafka.common.config.AbstractConfig".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (classOfAbstractConfig.equals(className)) {
            classVisitor = new KafkaAbstractConfigAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String getName() {
        return "kafka";
    }
}
