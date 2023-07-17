package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.common.scope.Scope;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.MethodContext;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmMethods;
import io.dongtai.iast.core.bytecode.enhance.asm.AsmTypes;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyNode;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.reflect.Modifier;

/**
 * @author dongzhiyong@huoxian.cn
 */
public abstract class AbstractAdviceAdapter extends AdviceAdapter implements AsmTypes, AsmMethods {
    protected String name;
    protected String desc;
    protected int access;
    protected Label tryLabel;
    protected Label catchLabel;
    protected ClassContext classContext;
    protected MethodContext context;
    protected String type;
    protected String signature;
    protected Type returnType;
    protected boolean hasException;

    public AbstractAdviceAdapter(MethodVisitor mv,
                                 int access,
                                 String name,
                                 String desc,
                                 ClassContext context,
                                 String type,
                                 String signCode) {
        super(AsmUtils.api, mv, access, name, desc);
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.classContext = context;

        this.returnType = Type.getReturnType(desc);
        this.tryLabel = new Label();
        this.catchLabel = new Label();
        this.type = type;
        this.signature = signCode;
        this.hasException = false;
    }

    public AbstractAdviceAdapter(MethodVisitor mv, int access, String name, String descriptor, String signature,
                                 MethodContext context) {
        super(AsmUtils.api, mv, access, name, descriptor);
        this.access = access;
        this.name = name;
        this.desc = descriptor;
        this.signature = signature;

        this.context = context;

        this.returnType = Type.getReturnType(descriptor);
        this.hasException = false;
    }


    @Override
    protected void onMethodEnter() {
        before();
    }

    /**
     * 方法退出时，调用此方法
     *
     * @param opcode
     */
    @Override
    protected void onMethodExit(final int opcode) {
        if (!isThrow(opcode)) {
            after(opcode);
        }
    }

    protected abstract void before();

    protected abstract void after(final int opcode);

    protected void loadThisOrPushNullIfIsStatic() {
        if (Modifier.isStatic(access)) {
            push((Type) null);
        } else {
            loadThis();
        }
    }

    /**
     * 方法结束前，如何判断是否需要throw、return，解决堆栈未对齐
     *
     * @param maxStack
     * @param maxLocals
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mark(catchLabel);
        visitTryCatchBlock(tryLabel, catchLabel, mark(), ASM_TYPE_THROWABLE.getInternalName());

        after(ATHROW);
        throwException();
        super.visitMaxs(maxStack, maxLocals);
    }

    public void visitMaxsNew(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
    }


    /**
     * 捕获当前方法的状态及数据
     *
     * @param opcode     当前操作码
     * @param policyNode hook点数据类型
     * @param captureRet 捕获返回值
     */
    public void trackMethod(
            final int opcode,
            final PolicyNode policyNode,
            final boolean captureRet
    ) {
        newLocal(ASM_TYPE_OBJECT);
        if (captureRet && !isThrow(opcode)) {
            loadReturn(opcode);
        } else {
            pushNull();
        }
        storeLocal(this.nextLocal - 1);
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThisOrPushNullIfIsStatic();
        loadArgArray();
        loadLocal(this.nextLocal - 1);
        push(policyNode.toString());
        push(this.context.getClassName());
        push(this.context.getMatchedClassName());
        push(this.name);
        push(this.signature);
        push(Modifier.isStatic(this.access));
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectMethod);
        pop();
    }

    public void captureMethodState(
            final int opcode,
            final int hookValue,
            final boolean captureRet
    ) {
        newLocal(ASM_TYPE_OBJECT);
        if (captureRet && !isThrow(opcode)) {
            loadReturn(opcode);
        } else {
            pushNull();
        }
        storeLocal(nextLocal - 1);
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThisOrPushNullIfIsStatic();
        loadArgArray();
        loadLocal(nextLocal - 1);
        push(type);
        push(classContext.getClassName());
        push(classContext.getMatchedClassName());
        push(name);
        push(signature);
        push(Modifier.isStatic(access));
        push(hookValue);
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$collectMethodPool);
        pop();
    }

    public void skipCollect(
            final int opcode,
            final PolicyNode policyNode,
            final boolean captureRet
    ) {
        newLocal(ASM_TYPE_OBJECT);
        if (captureRet && !isThrow(opcode)) {
            loadReturn(opcode);
        } else {
            pushNull();
        }
        storeLocal(this.nextLocal - 1);
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        loadThisOrPushNullIfIsStatic();
        loadArgArray();
        loadLocal(this.nextLocal - 1);
        push(policyNode.toString());
        push(this.context.getClassName());
        push(this.context.getMatchedClassName());
        push(this.name);
        push(this.signature);
        push(Modifier.isStatic(this.access));
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$skipCollect);
        pop();
    }

    /**
     * 是否抛出异常返回(通过字节码判断)
     *
     * @param opcode 操作码
     * @return true:以抛异常形式返回 / false:非抛异常形式返回(return)
     */
    protected boolean isThrow(int opcode) {
        return opcode == ATHROW;
    }


    /**
     * 加载返回值
     *
     * @param opcode 操作吗
     */
    protected void loadReturn(int opcode) {
        switch (opcode) {

            case RETURN: {
                pushNull();
                break;
            }

            case ARETURN: {
                dup();
                break;
            }

            case LRETURN:
            case DRETURN: {
                dup2();
                box(Type.getReturnType(methodDesc));
                break;
            }

            default: {
                dup();
                box(Type.getReturnType(methodDesc));
                break;
            }

        }
    }

    /**
     * 将NULL压入栈
     */
    final protected void pushNull() {
        push((Type) null);
    }

    protected void enterScope(Scope scope) {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(scope.getId());
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$enterScope);
    }

    protected void leaveScope(Scope scope) {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(scope.getId());
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$leaveScope);
    }

    protected void inScope(Scope scope) {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(scope.getId());
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$inScope);
    }

    protected void isFirstLevelScope(Scope scope) {
        invokeStatic(ASM_TYPE_SPY_HANDLER, SPY_HANDLER$getDispatcher);
        push(scope.getId());
        invokeInterface(ASM_TYPE_SPY_DISPATCHER, SPY$isFirstLevelScope);
    }
}
