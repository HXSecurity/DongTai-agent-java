package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.IastContext;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public interface DispatchPlugin {
    /**
     * 分发类访问器
     *
     * @param classVisitor 当前类的类访问器
     * @param context      当前类的上下文对象
     * @return ClassVisitor 命中的类访问起
     */
    ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context);

    /**
     * 判断是否命中当前插件，如果命中则返回命中插件的类名，否则返回null
     *
     * @return String 命中插件的类的全限定类名
     */
    String isMatch();
}
