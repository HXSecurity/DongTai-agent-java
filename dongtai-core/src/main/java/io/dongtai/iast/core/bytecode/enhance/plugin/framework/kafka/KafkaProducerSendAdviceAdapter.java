package io.dongtai.iast.core.bytecode.enhance.plugin.framework.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class KafkaProducerSendAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    private int localRecord;
    protected KafkaProducerSendAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        loadArg(0);
        localRecord = newLocal(ASM_TYPE_OBJECT);
        dup();
        storeLocal(localRecord);
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$kafkaBeforeSend);
        storeArg(0);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            newLocal(ASM_TYPE_OBJECT);
            dup();
            storeLocal(nextLocal - 1);
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            loadLocal(localRecord);
            loadLocal(nextLocal - 1);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$kafkaAfterSend);
        }
    }
}
