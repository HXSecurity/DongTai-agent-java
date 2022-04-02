package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

public class SpringKafkaMessageListenerContainerAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected SpringKafkaMessageListenerContainerAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterKafka);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveKafka);
        }
    }
}
