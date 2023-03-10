package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class DubboExchangeHandlerAdapter extends AbstractClassVisitor {
    public static final String DUBBO_EXCHANGE_HANDLE_REQUEST = "{package}.dubbo.remoting.exchange.support.header.HeaderExchangeHandler.handleRequest({package}.dubbo.remoting.exchange.ExchangeChannel,{package}.dubbo.remoting.exchange.Request)";

    private final String packageName;
    private final String handleRequestSign;

    public DubboExchangeHandlerAdapter(ClassVisitor classVisitor, ClassContext context, String packageName) {
        super(classVisitor, context);
        this.packageName = packageName;
        this.handleRequestSign = DUBBO_EXCHANGE_HANDLE_REQUEST.replace("{package}", this.packageName);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);

        if (this.handleRequestSign.equals(signCode)) {
            DongTaiLog.debug("Adding dubbo provider tracking by {}", signCode);
            mv = new DubboExchangeHandleRequestAdviceAdapter(mv, access, name, desc, signCode,
                    this.context, this.packageName);
            setTransformed();
        }
        return mv;
    }
}
