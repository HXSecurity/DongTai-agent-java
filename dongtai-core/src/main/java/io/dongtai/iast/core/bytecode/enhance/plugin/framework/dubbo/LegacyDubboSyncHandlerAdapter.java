package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class LegacyDubboSyncHandlerAdapter extends AbstractClassVisitor {
    private static final String LEGACY_DUBBO_SYNC_HANDLER_INVOKE = " com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper.invoke(com.alibaba.dubbo.rpc.Invocation)".substring(1);

    public LegacyDubboSyncHandlerAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);

        if (LEGACY_DUBBO_SYNC_HANDLER_INVOKE.equals(signCode)) {
            DongTaiLog.debug("Adding dubbo tracking by {}", signCode);
            mv = new LegacyDubboSyncHandlerInvokeAdviceAdapter(mv, access, name, desc, signCode, this.context);
            setTransformed();
        }
        return mv;
    }
}
