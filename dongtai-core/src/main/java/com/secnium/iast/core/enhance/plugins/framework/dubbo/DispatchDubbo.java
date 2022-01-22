package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
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
