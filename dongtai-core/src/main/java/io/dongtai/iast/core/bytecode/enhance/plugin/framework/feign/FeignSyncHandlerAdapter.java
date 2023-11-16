package io.dongtai.iast.core.bytecode.enhance.plugin.framework.feign;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class FeignSyncHandlerAdapter extends AbstractClassVisitor {
    //此HOOK点添加header有并发安全风险，故修改为executeAndDecode
//    private static final String FEIGN_SYNC_HANDLER_INVOKE = "feign.SynchronousMethodHandler.invoke(java.lang.Object[])";
    private static final String FEIGN_SYNC_HANDLER_INVOKE = "feign.SynchronousMethodHandler.executeAndDecode(feign.RequestTemplate,feign.Request$Options)";

    public FeignSyncHandlerAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);

        if (FEIGN_SYNC_HANDLER_INVOKE.equals(signCode)) {
            DongTaiLog.debug("Adding feign tracking for type {}.{}", context.getClassName(), name);
            mv = new FeignSyncHandlerInvokeAdviceAdapter(mv, access, name, desc, signCode, this.context);
            setTransformed();
        }
        return mv;
    }
}
