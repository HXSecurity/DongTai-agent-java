package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
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
    ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy);

    String getName();
}
