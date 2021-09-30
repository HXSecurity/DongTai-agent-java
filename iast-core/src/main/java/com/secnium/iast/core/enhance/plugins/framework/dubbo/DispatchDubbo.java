package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchDubbo implements DispatchPlugin {
    static final String CLASS_OF_DUBBO = " org.apache.dubbo.monitor.support.MonitorFilter".substring(1);

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String classname = context.getClassName();
        if (CLASS_OF_DUBBO.equals(classname)) {
            classVisitor = new DubboAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
