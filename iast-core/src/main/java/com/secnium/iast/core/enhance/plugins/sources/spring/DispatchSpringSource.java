package com.secnium.iast.core.enhance.plugins.sources.spring;

import com.secnium.iast.core.enhance.IASTContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchSpringSource implements DispatchPlugin {
    private final String ARGUMENT_RESOLVER = " org/springframework/web/method/support/HandlerMethodArgumentResolver".substring(1);
    private IASTContext context;
    private final Logger logger = LoggerFactory.getLogger(DispatchSpringSource.class);

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
        String className = isMatch();
        if (null != className) {
            logger.debug("current class {} hit rule \"spring argument\"", className);
            classVisitor = new ArgumentResolverVisitor(classVisitor, context);
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
        return this.context.getAncestors().contains(ARGUMENT_RESOLVER) ? this.context.getClassName() : null;
    }
}
