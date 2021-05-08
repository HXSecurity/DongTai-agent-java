package com.secnium.iast.core.enhance.plugins.sources.struts2;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import org.objectweb.asm.ClassVisitor;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

/**
 * 判断是否为Struts2相关的source点入口
 *
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchStruts2 implements DispatchPlugin {
    private final String MULTIPART_REQUEST = " org/apache/struts2/dispatcher/multipart/MultiPartRequest".substring(1);
    private IastContext context;
    private final Logger logger = LogUtils.getLogger(DispatchStruts2.class);

    /**
     * 分发类访问器
     *
     * @param classVisitor 当前类的类访问器
     * @param context      当前类的上下文对象
     * @return ClassVisitor 命中的类访问起
     */
    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        this.context = context;
        String className = isMatch();
        if (null != className) {
            logger.debug("current class {} hit rule \"spring argument\"", className);
            classVisitor = new MultiPartRequestVisitor(classVisitor, context);
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
        return this.context.getAncestors().contains(MULTIPART_REQUEST) ? this.context.getClassName() : null;
    }
}
