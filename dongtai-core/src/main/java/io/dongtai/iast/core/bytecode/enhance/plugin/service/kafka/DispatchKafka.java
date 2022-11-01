package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

public class DispatchKafka implements DispatchPlugin {
    private final String classOfKafkaProducer = " org.apache.kafka.clients.producer.KafkaProducer".substring(1);
    private final String classOfKafkaConsumer = " org.apache.kafka.clients.consumer.KafkaConsumer".substring(1);
    private final String classOfAbstractConfig = " org.apache.kafka.common.config.AbstractConfig".substring(1);
    private final String classOfSpringKafkaMessageListenerContainer = " org.springframework.kafka.listener.KafkaMessageListenerContainer$ListenerConsumer".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        String className = context.getClassName();

        if (classOfKafkaProducer.equals(className)) {
            classVisitor = new KafkaProducerAdapter(classVisitor, context);
        } else if (classOfKafkaConsumer.equals(className)) {
            classVisitor = new KafkaConsumerAdapter(classVisitor, context);
        } else if (classOfAbstractConfig.equals(className)) {
            classVisitor = new KafkaAbstractConfigAdapter(classVisitor, context);
        } else if (classOfSpringKafkaMessageListenerContainer.equals(className)) {
            classVisitor = new SpringKafkaMessageListenerContainerAdapter(classVisitor, context);
        }

        return classVisitor;
    }
}
