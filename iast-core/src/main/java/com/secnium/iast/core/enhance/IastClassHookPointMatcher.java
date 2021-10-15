package com.secnium.iast.core.enhance;

import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.matcher.ConfigMatcher;
import org.slf4j.Logger;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 判断类 是否允许hook
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastClassHookPointMatcher {

    private final Logger logger = LogUtils.getLogger(getClass());
    private final Instrumentation inst;

    public IastClassHookPointMatcher(Instrumentation inst) {
        this.inst = inst;
    }


    /**
     * 获取已加载的类
     *
     * @return 返回已加载的类的集合
     */
    private Iterator<Class<?>> iteratorForLoadedClasses() {
        return new Iterator<Class<?>>() {

            final Class<?>[] loaded = inst.getAllLoadedClasses();
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < loaded.length;
            }

            @Override
            public Class<?> next() {
                return loaded[pos++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    /**
     * 查找需要修改的类
     * 1.配置文件中定义的类
     * 2.全量用户类(todo)
     *
     * @param inst                用于操作字节码的instrumentation对象
     * @param isRemoveUnsupported 是否不hook不受支持的类
     * @return 需要修改的类的集合
     */
    public static List<Class<?>> findForRetransform(Instrumentation inst, boolean isRemoveUnsupported) {
        final List<Class<?>> classes = new ArrayList<Class<?>>();
        IastClassHookPointMatcher classHookManager = new IastClassHookPointMatcher(inst);
        final Iterator<Class<?>> itForLoaded = classHookManager.iteratorForLoadedClasses();
        while (itForLoaded.hasNext()) {
            final Class<?> clazz = itForLoaded.next();

            if (isRemoveUnsupported && !inst.isModifiableClass(clazz)) {
                if (classHookManager.logger.isDebugEnabled()) {
                    classHookManager.logger.debug("remove from findForReTransform, because class:" + clazz.getName() + " is unModifiable");
                }
                continue;
            }

            try {
                String className = clazz.getName().replace('.', '/');
                ClassLoader loader = clazz.getClassLoader();
                if (ConfigMatcher.isHookPoint(className, loader)) {
                    classes.add(clazz);
                    if (classHookManager.logger.isDebugEnabled()) {
                        classHookManager.logger.debug("findForReTransform: class " + clazz.getName() + " is added");
                    }
                } else {
                    if (classHookManager.logger.isDebugEnabled()) {
                        classHookManager.logger.debug("findForReTransform: class " + clazz.getName() + " is ignored");
                    }
                }
            } catch (Throwable cause) {
                // 在这里可能会遇到非常坑爹的模块卸载错误
                // 当一个URLClassLoader被动态关闭之后，但JVM已经加载的类并不知情（因为没有GC）
                // 所以当尝试获取这个类更多详细信息的时候会引起关联类的ClassNotFoundException等未知的错误（取决于底层ClassLoader的实现）
                // 这里没有办法穷举出所有的异常情况，所以catch Throwable来完成异常容灾处理
                // 当解析类出现异常的时候，直接简单粗暴的认为根本没有这个类就好了
                if (classHookManager.logger.isDebugEnabled()) {
                    classHookManager.logger.debug("remove from findForReTransform, because loading class:" + clazz.getName() + " occur an exception", cause);
                }
            }
        }
        return classes;
    }

}
