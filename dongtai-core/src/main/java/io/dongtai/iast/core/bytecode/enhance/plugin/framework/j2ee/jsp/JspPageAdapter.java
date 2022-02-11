package io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.jsp;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch.ServletDispatcherAdviceAdapter;
import io.dongtai.iast.core.utils.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import io.dongtai.log.DongTaiLog;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class JspPageAdapter extends AbstractClassVisitor {


    JspPageAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("_jspService".equals(name)) {
            String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassName(), name, desc);
            mv = new JspAdviceAdapter(mv, access, name, desc, iastMethodSignature, context);
            transformed = true;
        }
        return mv;
    }

    private class JspAdviceAdapter extends ServletDispatcherAdviceAdapter {

        JspAdviceAdapter(MethodVisitor methodVisitor, int access, String name, String desc, String signature, IastContext context) {
            super(methodVisitor, access, name, desc, signature, context, false);
        }

        @Override
        public void visitMethodInsn(int opc, String owner, String name, String desc, boolean isInterface) {
            if (owner.endsWith("JspRuntimeLibrary") && "include".equals(name)) {
                if (DongTaiLog.isDebugEnabled()) {
                    DongTaiLog.debug("[com.secnium.iast] enter include method" + owner + "." + name);
                }

                int j = newLocal(Type.getType(Object.class));
                int k = newLocal(Type.getType(Object.class));
                int m = newLocal(Type.getType(String.class));
                int n = newLocal(Type.getType(Object.class));
                int i1 = newLocal(Type.BOOLEAN_TYPE);

                storeLocal(i1);// 出入对象
                storeLocal(n);
                storeLocal(m);
                storeLocal(k);
                storeLocal(j);
                //loadLocal(m);//读取数据

                loadLocal(j);
                loadLocal(k);
                loadLocal(m);
                loadLocal(n);
                loadLocal(i1);
            }
            super.visitMethodInsn(opc, owner, name, desc, isInterface);
        }
    }
}
