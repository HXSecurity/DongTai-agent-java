package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class LegacyDubboExchangeHandlerAdapter extends AbstractClassVisitor {
    public static final String LEGACY_DUBBO_EXCHANGE_HANDLE_REQUEST = " com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeHandler.handleRequest(com.alibaba.dubbo.remoting.exchange.ExchangeChannel,com.alibaba.dubbo.remoting.exchange.Request)".substring(1);

    public LegacyDubboExchangeHandlerAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);

        if (LEGACY_DUBBO_EXCHANGE_HANDLE_REQUEST.equals(signCode)) {
            DongTaiLog.debug("Adding dubbo provider tracking by {}", signCode);
            mv = new LegacyDubboExchangeHandleRequestAdviceAdapter(mv, access, name, desc, signCode, this.context);
            setTransformed();
        }
        return mv;
    }
}
