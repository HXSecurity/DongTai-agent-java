package com.secnium.iast.core.enhance.plugins;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.asm.AsmMethods;
import com.secnium.iast.core.enhance.asm.AsmTypes;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
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
    protected IastContext context;
    protected String type;
    protected String signature;
    protected Type returnType;
    protected boolean hasException;

    public AbstractAdviceAdapter(MethodVisitor mv,
                                 int access,
                                 String name,
                                 String desc,
                                 IastContext context,
                                 String type,
                                 String signCode) {
        super(AsmUtils.api, mv, access, name, desc);
        System.out.println("AbstractAdviceAdapter-333333333333333333333333");
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.context = context;

        this.returnType = Type.getReturnType(desc);
        this.tryLabel = new Label();
        this.catchLabel = new Label();
        this.type = type;
        this.signature = signCode;
        this.hasException = false;
        System.out.println("method name="+name);
    }


    @Override
    protected void onMethodEnter() {
        System.out.println("onMethodEnter-2222222222222222222222222222");
        before();
    }

    /**
     * 方法退出时，调用此方法
     *
     * @param opcode
     */
    @Override
    protected void onMethodExit(final int opcode) {
        System.out.println("onMethodExit-1111111111111111111111111111");
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

    /**
     * 捕获当前方法的状态及数据
     *
     * @param opcode     当前操作码
     * @param hookValue  hook点数据类型
     * @param captureRet 捕获返回值
     */
    protected void captureMethodState(
            final int opcode,
            final int hookValue,
            final boolean captureRet
    ) {
        if (captureRet && !isThrow(opcode)) {
            loadReturn(opcode);
        } else {
            pushNull();
        }
        loadArgArray();
        push(context.getNamespace());
        push(type);
        push(context.getListenId());
        push(context.getMatchClassname());
        System.out.println("getMatchClassname="+context.getMatchClassname());
        push(name);
        push(desc);
        System.out.println("desc="+desc);
        loadThisOrPushNullIfIsStatic();
        push(signature);
        push(Modifier.isStatic(access));
        push(hookValue);
        System.out.println("ASM_METHOD_Spy$spyMethodOnBefore="+ASM_METHOD_Spy$spyMethodOnBefore);
        invokeStatic(ASM_TYPE_SPY, ASM_METHOD_Spy$spyMethodOnBefore);
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

}
