package com.secnium.iast.core.enhance.plugins.sinks.autobinding;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.report.ErrorLogReport;
import com.secnium.iast.core.util.AsmUtils;
import com.secnium.iast.core.util.ReflectUtils;
import com.secnium.iast.core.util.ThrowableUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;


/**
 * @author dongzhiyong@huoxian.cn
 */
public class SpringAutoBindingAdapter extends AbstractClassVisitor {

    private static Method onbind;
    private static Type classtype;

    public SpringAutoBindingAdapter(ClassVisitor classVisitor, IastContext context) {
        super(classVisitor, context);
    }

    @Override
    public boolean hasTransformed() {
        return transformed;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        SpringAutoBindingAdviceAdapter methodVisitor = (SpringAutoBindingAdviceAdapter) super.visitMethod(access,
                name,
                descriptor,
                signature,
                exceptions);

        if ("doBind".equals(name) && Type.getArgumentTypes(descriptor).length == 2) {
            methodVisitor = new SpringAutoBindingAdviceAdapter(methodVisitor,
                    access,
                    name,
                    descriptor);
            transformed = true;
        }
        return methodVisitor;
    }

    private static class SpringAutoBindingAdviceAdapter extends AdviceAdapter {

        SpringAutoBindingAdviceAdapter(MethodVisitor methodVisitor,
                                       int access,
                                       String name,
                                       String descriptor) {
            super(AsmUtils.api, methodVisitor, access, name, descriptor);
        }

        @Override
        public void onMethodEnter() {
            loadThis();
            loadArg(0);
            // 设置进入方法时，执行的动作
            invokeStatic(classtype, onbind);
        }
    }

    static {
        try {
            classtype = Type.getType(SpringAutoBindingDispatchImpl.class);
            onbind = Method.getMethod(ReflectUtils.getPublicMethodFromClass(SpringAutoBindingDispatchImpl.class, "onDoBind"));
        } catch (NoSuchMethodException e) {
            ErrorLogReport.sendErrorLog(ThrowableUtils.getStackTrace(e));
        }

    }
}
