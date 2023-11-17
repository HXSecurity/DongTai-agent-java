package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.utils.AsmUtils;
import io.dongtai.log.DongTaiLog;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * @author mazepeng
 * @date 2023/11/2 17:40
 */
public class DubboHessianAdapter extends AbstractClassVisitor {

    private static final String HESSIAN_ADDREQUESTHEADERS = " com.caucho.hessian.client.HessianProxy.addRequestHeaders(com.caucho.hessian.client.HessianConnection)".substring(1);

    public DubboHessianAdapter(ClassVisitor classVisitor, ClassContext context) {
        super(classVisitor, context);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        String signCode = AsmUtils.buildSignature(context.getClassName(), name, descriptor);
        if (HESSIAN_ADDREQUESTHEADERS.equals(signCode)) {
            DongTaiLog.debug("Adding dubbo provider source tracking by {}", signCode);
            System.out.println("hessian增强完成");
            mv = new DubboHessianAddRequestHeadersAdapter(mv, access, name, descriptor,this.context,"hessian",signCode);
            setTransformed();
        }
        return mv;    }
}
