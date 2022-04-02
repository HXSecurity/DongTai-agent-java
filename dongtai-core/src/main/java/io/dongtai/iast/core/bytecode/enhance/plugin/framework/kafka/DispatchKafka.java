package io.dongtai.iast.core.bytecode.enhance.plugin.framework.kafka;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

public class DispatchKafka implements DispatchPlugin {
    private final String classOfKafkaProducer = " org.apache.kafka.clients.producer.KafkaProducer".substring(1);
    private final String classOfKafkaConsumer = " org.apache.kafka.clients.consumer.KafkaConsumer".substring(1);
    private final String classOfSpringKafkaMessageListenerContainer = " org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String className = context.getClassName();

        if (classOfKafkaProducer.equals(className)) {
            classVisitor = new KafkaProducerAdapter(classVisitor, context);
        } else if (classOfKafkaConsumer.equals(className)) {
            classVisitor = new KafkaConsumerAdapter(classVisitor, context);
        } else if (classOfSpringKafkaMessageListenerContainer.equals(className)) {
            classVisitor = new SpringKafkaMessageListenerContainerAdapter(classVisitor, context);
        }

        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
