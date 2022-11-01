package io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchDubbo implements DispatchPlugin {

    static final String CLASS_OF_DUBBO = " .dubbo.monitor.support.MonitorFilter".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        if (context.getClassName().endsWith(CLASS_OF_DUBBO)) {
            context.setMatchedClassName(context.getClassName());
            classVisitor = new DubboAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
