package com.secnium.iast.core.enhance.plugins.framework.dubbo;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchDubbo implements DispatchPlugin {

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        String classname = context.getClassName();
        if (" com/alibaba/dubbo/rpc/proxy/javassist/JavassistProxyFactory$1".substring(1).equals(classname)) {
            classVisitor = new DubboAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }
}
