package io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka;

import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.service.ServiceType;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;

public class KafkaConsumerAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    private int localServers;
    private int localServersString;
    private int localUrlHandler;
    protected KafkaConsumerAdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(AsmUtils.api, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != ATHROW) {
            localServers = newLocal(Type.getType(ArrayList.class));
            loadArg(0);
            push("bootstrap.servers");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, "java/util/ArrayList");
            storeLocal(localServers);

            localServersString = newLocal(Type.getType(String.class));
            push(",");
            loadLocal(localServers);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "join", "(Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;", false);
            storeLocal(localServersString);

            localUrlHandler = newLocal(ASM_TYPE_OBJECT);
            mv.visitTypeInsn(NEW, "io/dongtai/iast/core/handler/hookpoint/service/url/KafkaUrlHandler");
            dup();
            mv.visitMethodInsn(INVOKESPECIAL, "io/dongtai/iast/core/handler/hookpoint/service/url/KafkaUrlHandler",
                    "<init>", "()V", false);
            storeLocal(localUrlHandler);

            invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
            push(ServiceType.KAFKA.getCategory());
            push(ServiceType.KAFKA.getType());
            loadLocal(localServersString);
            push("");
            loadLocal(localUrlHandler);
            invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$reportService);
        }
    }
}
