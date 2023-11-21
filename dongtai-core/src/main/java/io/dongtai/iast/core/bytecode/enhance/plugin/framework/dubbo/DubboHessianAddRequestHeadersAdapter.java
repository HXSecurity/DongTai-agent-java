package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractAdviceAdapter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * @author mazepeng
 * @date 2023/11/2 17:40
 */
public class DubboHessianAddRequestHeadersAdapter  extends AbstractAdviceAdapter {


    private Label exHandler;

    private final Type urlType;
    private static final Method URL_TO_STRING_METHOD = Method.getMethod("java.lang.String toString()");


    public DubboHessianAddRequestHeadersAdapter(MethodVisitor mv, int access, String name, String desc, ClassContext context, String type, String signCode) {
        super(mv, access, name, desc, context, type, signCode);
        this.urlType = Type.getObjectType("java/net/URL");


    }
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitLabel(this.catchLabel);
        visitLabel(this.exHandler);
        leaveMethod(ATHROW);
        throwException();
        visitTryCatchBlock(this.tryLabel, this.catchLabel, this.exHandler, ASM_TYPE_THROWABLE.getInternalName());
        super.visitMaxsNew(maxStack, maxLocals);
    }

    @Override
    protected void onMethodEnter() {
        this.tryLabel = new Label();
        visitLabel(this.tryLabel);
        enterMethod();
        this.catchLabel = new Label();
        this.exHandler = new Label();
    }

    @Override
    protected void onMethodExit(int opcode) {
        leaveMethod(opcode);
    }
    private void leaveMethod(int opcode) {
        leaveScope();
    }
    private void leaveScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(false);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leavePropagator);
    }


    private void enterMethod() {
        skipCollect();
        enterScope();
        Label endLabel = new Label();
        traceMethod();
        mark(endLabel);
    }

    @Override
    protected void before() {
    }

    @Override
    protected void after(int opcode) {
    }

    private void skipCollect() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadArg(0);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER,SPY$isSkipCollectDubbo);
        pop();
    }

    private void enterScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(false);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterPropagator);
    }

    private void isFirstScope() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelPropagator);
    }

    private void traceMethod() {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);

        //加载this 1
        loadThis();
        //2
        dup();
        visitFieldInsn(Opcodes.GETFIELD,"com/caucho/hessian/client/HessianProxy","_url","Ljava/net/URL;");
        invokeVirtual(this.urlType,URL_TO_STRING_METHOD);
        //参数 3
        loadArg(0);
        //arguments 4
        pushNull();
        //headers 5
        pushNull();
        //6
        push(this.classContext.getClassName());
        //7
        push(this.name);
        // 8
        push(this.signature);
        //获取静态getDispatcher
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$traceDubboInvoke);
    }
}
