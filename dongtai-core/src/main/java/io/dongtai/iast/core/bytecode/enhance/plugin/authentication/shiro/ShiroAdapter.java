package io.dongtai.iast.core.bytecode.enhance.plugin.authentication.shiro;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;


public class ShiroAdapter extends AbstractClassVisitor {

    public ShiroAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor = super.visitMethod(access,
                name,
                descriptor,
                signature,
                exceptions);
        if ("readSession".equals(name)) {
            generateNewBody(methodVisitor);
            return null;
        }
        return methodVisitor;
    }

    protected void generateNewBody(MethodVisitor methodVisitor) {
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(169, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, " org/apache/shiro/session/mgt/eis/AbstractSessionDAO".substring(1), "doReadSession", " (Ljava/io/Serializable;)Lorg/apache/shiro/session/Session;".substring(1), false);
        methodVisitor.visitVarInsn(ASTORE, 2);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(170, label1);
        methodVisitor.visitVarInsn(ALOAD, 2);
        Label label2 = new Label();
        methodVisitor.visitJumpInsn(IFNONNULL, label2);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(171, label3);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/dongtai/SpyDispatcherHandler", "getDispatcher", "()Ljava/lang/dongtai/SpyDispatcher;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/lang/dongtai/SpyDispatcher", "isReplayRequest", "()Z", true);
        Label label4 = new Label();
        methodVisitor.visitJumpInsn(IFEQ, label4);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLineNumber(172, label5);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, " org/apache/shiro/session/mgt/eis/AbstractSessionDAO".substring(1), "getActiveSessions", "()Ljava/util/Collection;", false);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/Collection", "stream", "()Ljava/util/stream/Stream;", true);
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/Stream", "findFirst", "()Ljava/util/Optional;", true);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false);
        methodVisitor.visitTypeInsn(CHECKCAST, " org/apache/shiro/session/Session".substring(1));
        methodVisitor.visitMethodInsn(INVOKEINTERFACE, " org/apache/shiro/session/Session".substring(1), "getId", "()Ljava/io/Serializable;", true);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, " org/apache/shiro/session/mgt/eis/AbstractSessionDAO".substring(1), "doReadSession", " (Ljava/io/Serializable;)Lorg/apache/shiro/session/Session;".substring(1), false);
        methodVisitor.visitVarInsn(ASTORE, 2);
        Label label6 = new Label();
        methodVisitor.visitLabel(label6);
        methodVisitor.visitLineNumber(173, label6);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(175, label4);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{" org/apache/shiro/session/Session".substring(1)}, 0, null);
        methodVisitor.visitTypeInsn(NEW, " org/apache/shiro/session/UnknownSessionException".substring(1));
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        methodVisitor.visitLdcInsn("There is no session with id [");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitLdcInsn("]");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, " org/apache/shiro/session/UnknownSessionException".substring(1), "<init>", "(Ljava/lang/String;)V", false);
        methodVisitor.visitInsn(ATHROW);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(177, label2);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ARETURN);
        Label label7 = new Label();
        methodVisitor.visitLabel(label7);
        methodVisitor.visitLocalVariable("this", "Lorg/apache/shiro/session/mgt/eis/AbstractSessionDAO;", null, label0, label7, 0);
        methodVisitor.visitLocalVariable("sessionId", "Ljava/io/Serializable;", null, label0, label7, 1);
        methodVisitor.visitLocalVariable("s", " Lorg/apache/shiro/session/Session;".substring(1), null, label1, label7, 2);
        methodVisitor.visitMaxs(4, 3);
        methodVisitor.visitEnd();
        setTransformed();
    }

}



