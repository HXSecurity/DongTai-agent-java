package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.service.ServiceType;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.List;

public class KafkaAbstractConfigInitAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    private int localServers;
    private int localServersString;
    protected KafkaAbstractConfigInitAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            Label tryL = new Label();
            Label catchL = new Label();
            Label exHandlerL = new Label();
            visitTryCatchBlock(tryL, catchL, exHandlerL, ASM_TYPE_THROWABLE.getInternalName());
            visitLabel(tryL);

            localServers = newLocal(Type.getType(List.class));
            loadThis();
            push("bootstrap.servers");
            mv.visitMethodInsn(INVOKEVIRTUAL, " org/apache/kafka/common/config/AbstractConfig".substring(1), "getList", "(Ljava/lang/String;)Ljava/util/List;", false);
            storeLocal(localServers);

            localServersString = newLocal(Type.getType(String.class));
            push(",");
            loadLocal(localServers);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "join", "(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;", false);
            storeLocal(localServersString);

            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(ServiceType.KAFKA.getCategory());
            push(ServiceType.KAFKA.getType());
            loadLocal(localServersString);
            push("");
            push("KafkaUrlHandler");
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$reportService);

            visitLabel(catchL);
            Label endL = new Label();
            visitJumpInsn(GOTO, endL);
            visitLabel(exHandlerL);
            visitVarInsn(ASTORE, this.nextLocal);
            visitLabel(endL);
        }
    }
}
