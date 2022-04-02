package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class KafkaConsumerPollAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected KafkaConsumerPollAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            newLocal(ASM_TYPE_OBJECT);
            dup();
            storeLocal(nextLocal - 1);
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            loadLocal(nextLocal - 1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$kafkaAfterPoll);
        }
    }
}
