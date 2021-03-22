package com.secnium.iast.core.enhance.plugins.sources.servlet.stream.catalina;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchCoyoteInputStream implements DispatchPlugin {
    private final String BASE_CLASS = " org/apache/catalina/connector/CoyoteInputStream".substring(1);
    private IASTContext context;

    /**
     * 分发类访问器
     *
     * @param classVisitor 当前类的类访问器
     * @param context      当前类的上下文对象
     * @return ClassVisitor 命中的类访问起
     */
    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IASTContext context) {
        this.context = context;
        String classname = isMatch();
        if (null != classname) {
            classVisitor = new CoyoteInputStreamClassVisitor(classVisitor, context);
        }
        return classVisitor;
    }

    /**
     * 判断是否命中当前插件，如果命中则返回命中插件的类名，否则返回null
     *
     * @return String 命中插件的类的全限定类名
     */
    @Override
    public String isMatch() {
        return this.context.getClassName().equals(BASE_CLASS) ? BASE_CLASS : null;
    }
}
