package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchDubbo implements DispatchPlugin {

    static final String CLASS_OF_DUBBO = " .dubbo.monitor.support.MonitorFilter".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        if (context.getClassName().endsWith(CLASS_OF_DUBBO)) {
            context.setMatchClassName(context.getClassName());
            classVisitor = new DubboAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
