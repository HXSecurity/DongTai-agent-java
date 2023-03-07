package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class DubboExchangeChannelAdapter extends AbstractClassVisitor {
    public static final String DUBBO_EXCHANGE_CHANNEL_SEND = "{package}.dubbo.remoting.exchange.support.header.HeaderExchangeChannel.send(java.lang.Object)";

    private final String packageName;
    private final String sendSign;

    public DubboExchangeChannelAdapter(ClassVisitor classVisitor, ClassContext context, String packageName) {
        super(classVisitor, context);
        this.packageName = packageName;
        this.sendSign = DUBBO_EXCHANGE_CHANNEL_SEND.replace("{package}", this.packageName);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, desc);

        if (this.sendSign.equals(signCode)) {
            DongTaiLog.debug("Adding dubbo provider response tracking by {}", signCode);
            mv = new DubboExchangeChannelSendAdviceAdapter(mv, access, name, desc, signCode,
                    this.context, this.packageName);
            setTransformed();
        }
        return mv;
    }
}
