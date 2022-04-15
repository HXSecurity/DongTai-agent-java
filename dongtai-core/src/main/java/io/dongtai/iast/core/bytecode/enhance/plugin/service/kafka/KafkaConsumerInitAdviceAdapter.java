package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.service.ServiceType;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class KafkaConsumerInitAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    private int localServersString;
    protected KafkaConsumerInitAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            localServersString = newLocal(Type.getType(String.class));
            loadArg(0);
            push("bootstrap.servers");
            mv.visitMethodInsn(INVOKEINTERFACE, " org/apache/kafka/clients/consumer/ConsumerConfig".substring(1),
                    "getString", "(Ljava/lang/String;)Ljava/lang/String;", false);
            storeLocal(localServersString);

            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(ServiceType.KAFKA.getCategory());
            push(ServiceType.KAFKA.getType());
            loadLocal(localServersString);
            push("");
            push("KafkaUrlHandler");
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$reportService);
        }
    }
}
